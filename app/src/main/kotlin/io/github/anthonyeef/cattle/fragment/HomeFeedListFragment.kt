package io.github.anthonyeef.cattle.fragment

import android.os.Bundle
import android.view.View
import io.github.anthonyeef.cattle.activity.PhotoDisplayActivity
import io.github.anthonyeef.cattle.constant.app
import io.github.anthonyeef.cattle.contract.HomeFeedListContract
import io.github.anthonyeef.cattle.data.statusData.Status
import io.github.anthonyeef.cattle.entity.BottomRefreshEntity
import io.github.anthonyeef.cattle.exception.showException
import io.github.anthonyeef.cattle.presenter.HomeFeedListPresenter
import io.github.anthonyeef.cattle.utils.LoadMoreDelegate
import io.github.anthonyeef.cattle.utils.SwipeRefreshDelegate
import io.github.anthonyeef.cattle.viewbinder.BottomRefreshItemViewBinder
import io.github.anthonyeef.cattle.viewbinder.FeedStatusItemViewBinder
import me.drakeet.multitype.Items
import me.drakeet.multitype.register
import org.jetbrains.anko.intentFor

/**
 *
 */
class HomeFeedListFragment : BaseListFragment(),
    HomeFeedListContract.View,
    SwipeRefreshDelegate.OnSwipeRefreshListener,
    LoadMoreDelegate.LoadMoreSubject, FeedStatusItemViewBinder.FeedStatusItemCallback {

  private lateinit var homeFeedListPresenter: HomeFeedListContract.Presenter
  private lateinit var refreshDelegate: SwipeRefreshDelegate
  private lateinit var loadMoreDelegate: LoadMoreDelegate

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    adapter.register(FeedStatusItemViewBinder().registerCallback(this))
    adapter.register(BottomRefreshItemViewBinder())
    refreshDelegate = SwipeRefreshDelegate(this)
    loadMoreDelegate = LoadMoreDelegate(this)
    HomeFeedListPresenter(this)
  }


  override fun onResume() {
    super.onResume()
    homeFeedListPresenter.subscribe()
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    refreshDelegate.attach(this)
    loadMoreDelegate.attach(this)
  }


  override fun onPause() {
    super.onPause()
    homeFeedListPresenter.unSubscribe()
  }


  override fun setPresenter(presenter: HomeFeedListContract.Presenter) {
    homeFeedListPresenter = presenter
  }


  override fun updateTimeline(clearData: Boolean, data: List<Status>, showAnimation: Boolean) {
    if (clearData) {
      items = Items(data)
      adapter.items = items
      adapter.notifyDataSetChanged()
      if (showAnimation) {
        runLayoutAnimation()
      }
    } else {
      val tempItems = items
      val position = tempItems.size
      items.addAll(data)
      adapter.notifyItemInserted(position)
    }
  }


  override fun onSwipeRefresh() {
    homeFeedListPresenter.loadDataFromRemote(clearData = true)
  }


  override fun showError(e: Throwable) {
    showException(this, e)
  }


  override fun setLoadingProgressBar(show: Boolean) {
    refreshDelegate.setRefresh(show)
  }


  override fun scrollToTop() {
    refreshDelegate.scrollToTop()
  }


  override fun isActivated(): Boolean {
    return isAdded
  }


  override fun isLoading(): Boolean {
    return homeFeedListPresenter.isLoading()
  }


  // 虽然写了，但感觉用不到。先放着吧。
  override fun setBottomLoadingProgressBar(show: Boolean) {
    if (show) {
      if (items[items.size - 1] !is BottomRefreshEntity) {
        items.add(BottomRefreshEntity("Test"))
        adapter.notifyDataSetChanged()
      }
    } else {
      if (items[items.size - 1] is BottomRefreshEntity) {
        items.removeAt(items.size - 1)
      }
      adapter.notifyDataSetChanged()
    }
  }


  override fun onLoadMore() {
    if (!isLoading()) {
      homeFeedListPresenter.loadDataFromRemote(false)
    }
  }


  override fun onClickedPhoto(status: Status, photoView: View) {
    val isGif = status.photo?.largeurl?.endsWith("gif", ignoreCase = true) == true
    val intent = app.intentFor<PhotoDisplayActivity>(PhotoDisplayActivity.KEY_IMAGE_URL to status.photo?.largeurl, PhotoDisplayActivity.KEY_IS_GIF to isGif)

    activity?.let {
      startActivity(intent)
    }
  }
}