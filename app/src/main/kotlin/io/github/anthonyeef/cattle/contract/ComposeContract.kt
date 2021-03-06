package io.github.anthonyeef.cattle.contract

import io.github.anthonyeef.cattle.BasePresenter
import io.github.anthonyeef.cattle.BaseView

/**
 * Contract for compose presenter and view
 */
interface ComposeContract {
    interface View : BaseView<Presenter> {
        fun sendFinish(success: Boolean)
        fun getStatus(): String
    }

    interface Presenter : BasePresenter {
        fun sendFanfou()
    }
}