package io.github.anthonyeef.cattle.presenter

import android.net.Uri
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.github.anthonyeef.cattle.constant.*
import io.github.anthonyeef.cattle.contract.LoginContract
import io.github.anthonyeef.cattle.exception.ApiException
import io.github.anthonyeef.cattle.exception.DataNotFoundException
import io.github.anthonyeef.cattle.extension.getQueryParameter
import io.github.anthonyeef.cattle.extension.realm.save
import io.github.anthonyeef.cattle.service.AccountService
import io.github.anthonyeef.cattle.service.LoginService
import io.github.anthonyeef.cattle.service.ServiceGenerator
import io.github.anthonyeef.cattle.service.model.UserInfo
import io.github.anthonyeef.cattle.utils.CatLogger
import io.github.anthonyeef.cattle.utils.SharedPreferenceUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info

/**
 * Presenter for login.
 */
class LoginPresenter() : LoginContract.Presenter, CatLogger {
    lateinit var loginService: LoginService
    lateinit var loginView: LoginContract.View
    var _disposable: CompositeDisposable = CompositeDisposable()
    var accessToken: String = ""
    var accessTokenSecret: String = ""

    constructor(view: LoginContract.View) : this() {
        loginView = view
        loginView.setPresenter(this)
    }

    override fun subscribe() {
        if (loginView.isActive()) {
            // TODO
        }
    }

    override fun unSubscribe() {
        _disposable.clear()
    }

    override fun login() {
        loginService = ServiceGenerator.createService(LoginService::class.java,
                token = "", secret = "")
        _disposable.add(loginService.oauthRequestToken(FanfouRequestTokenUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { loginView.goAuthorizeRequestToken() }
                .subscribe({ result -> saveToken(result) }, { error ->
                    if (error is HttpException) {
                        loginView.onError(ApiException(error.code(), error.message()))
                    } else {
                        loginView.onError(error)
                    }
                }))
    }

    override fun fetchAccessToken() {
        _disposable.add(ServiceGenerator.createDefaultService(AccountService::class.java)
                .oauthAccessToken(FanfouAccessTokenUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response -> saveToken(response) }, { error -> loginView.onError(error) }))
    }

    override fun checkCredential() {
        _disposable.add(ServiceGenerator.createDefaultService(AccountService::class.java)
                .verifyCredential()
                .doOnNext { saveUserInfo(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ loginView.showLoginSuccess() }, { error -> loginView.onError(error) }))
    }

    override fun getLoginAddress(): Uri? {
        if (accessToken.isNullOrBlank()) {
            loginView.onError(DataNotFoundException("Token not found"))
            return null
        } else {
            return Uri.parse(FanfouLoginBaseUrl)
                    .buildUpon()
                    .appendQueryParameter("oauth_token", accessToken)
                    .appendQueryParameter("oauth_callback", CattleOauthCallbackUrl)
                    .build()
        }
    }

    private fun saveToken(response: String) {
        accessToken = response.getQueryParameter("oauth_token")
        accessTokenSecret = response.getQueryParameter("oauth_token_secret")
        SharedPreferenceUtils.putString(KEY_ACCESS_TOKEN, accessToken)
        SharedPreferenceUtils.putString(KEY_ACCESS_TOKEN_SECRET, accessTokenSecret)
    }

    private fun saveUserInfo(user: UserInfo) {
        SharedPreferenceUtils.putString(KEY_CURRENT_USER_ID, user.id)
        user.save()
        info("Save userInfo done")
    }
}