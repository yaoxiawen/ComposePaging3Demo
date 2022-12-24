package com.example.composepaging3demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.example.composepaging3demo.ui.theme.ComposePaging3DemoTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePaging3DemoTheme {
                RefreshLoadUse2()
            }
        }
    }

    @Composable
    fun SimpleUse() {
        val viewModel: SimpleUseViewModel = viewModel()
        val datas = viewModel.recipesPager.collectAsLazyPagingItems()
        LazyColumn(content = {
            itemsIndexed(datas) { _, data ->
                Box(
                    Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(1.dp, Color.Red, RoundedCornerShape(5.dp))
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = data?.data ?: "")
                }
            }
        })
    }


    @Composable
    fun RefreshLoadUse() {
        val refreshState = rememberSwipeRefreshState(isRefreshing = false)
        val viewModel: SimpleUseViewModel = viewModel()
        val collectAsLazyPagingItems = viewModel.recipesPager.collectAsLazyPagingItems()
        SwipeRefresh(state = refreshState, onRefresh = {
            collectAsLazyPagingItems.refresh()
        }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                content = {
                    itemsIndexed(collectAsLazyPagingItems) { index, refreshData ->//每个item的展示
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color.Green, shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color.Red,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = refreshData?.data ?: "")
                        }
                    }
                    when (collectAsLazyPagingItems.loadState.append) {
                        is LoadState.Loading -> {//加载中的尾部item展示
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "加载中。。。")
                                }
                            }
                        }
                        else -> {//加载完成或者加载错误展示的尾部item
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "--加载完成或加载错误--")
                                }
                            }
                        }
                    }
                })
        }
    }

    @Composable
    fun RefreshLoadUse2() {
        val viewModel: SimpleUseViewModel = viewModel()
        val collectAsLazyPagingItems = viewModel.recipesPager.collectAsLazyPagingItems()
        SwipeRefreshList(collectAsLazyPagingItems) {
            itemsIndexed(collectAsLazyPagingItems) { index, refreshData ->//每个item的展示
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.Green, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = refreshData?.data ?: "")
                }
            }
        }
    }

    /**
     * 下拉加载封装
     * 参数1：LazyPagingItems包装的请求结果，可以存储在ViewModel,从ViewMode获取
     * 参数2：列表内容 listContent 需要外部传入需要携带上下文LazyListScope，可复用
     * */
    @Composable
    fun <T : Any> SwipeRefreshList(
        collectAsLazyPagingItems: LazyPagingItems<T>,
        listContent: LazyListScope.() -> Unit,
    ) {
        val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
        SwipeRefresh(
            state = rememberSwipeRefreshState,
            onRefresh = { collectAsLazyPagingItems.refresh() }
        ) {

            rememberSwipeRefreshState.isRefreshing =
                collectAsLazyPagingItems.loadState.refresh is LoadState.Loading

            // lazyColumn的状态属性
            val lazyListState = rememberLazyListState()

            // 定义一个协程作用域用来跳到列表顶部
            val coroutineScope = rememberCoroutineScope()

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                ) {

                // 具体的列表内容，从父节点参数传入
                listContent()

                collectAsLazyPagingItems.apply {
                    when {
                        loadState.append is LoadState.Loading -> {
                            //加载更多，底部loading
                            item { LoadingItem() }
                        }
                        loadState.append is LoadState.Error -> {
                            //加载更多异常
                            item {
                                ErrorMoreRetryItem() {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        }


                        loadState.append == LoadState.NotLoading(endOfPaginationReached = true) -> {
                            // 已经没有更多数据了
                            item {
                                NoMoreDataFindItem(onClick = {
                                    // 点击事件 跳到列表顶部
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(0)
                                    }
                                })
                            }
                        }

                        loadState.refresh is LoadState.Error -> {
                            if (collectAsLazyPagingItems.itemCount <= 0) {
                                // 刷新的时候，如果itemCount小于0，第一次加载异常
                                item {
                                    ErrorContent() {
                                        collectAsLazyPagingItems.retry()
                                    }
                                }
                            } else {
                                item {
                                    ErrorMoreRetryItem() {
                                        collectAsLazyPagingItems.retry()
                                    }
                                }
                            }
                        }
                        loadState.refresh is LoadState.Loading -> {
                            // 第一次加载且正在加载中
                            if (collectAsLazyPagingItems.itemCount == 0) {
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 底部加载更多失败处理
     * */
    @Composable
    fun ErrorMoreRetryItem(retry: () -> Unit) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(
                onClick = { retry() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(3.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                ),
            ) {
                Text(text = "请重试", color = Color.DarkGray)
            }
        }
    }

    /**
     * 页面加载失败处理
     * */
    @Composable
    fun ErrorContent(retry: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Image(
//                modifier = Modifier.padding(top = 80.dp),
//                painter = painterResource(id = R.drawable.ic_default_empty),
//                contentDescription = null
//            )
            Text(text = "请求失败，请检查网络", modifier = Modifier.padding(8.dp))
            TextButton(
                onClick = { retry() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(5.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                )
                //colors = ButtonDefaults
            ) { Text(text = "重试", color = Color.Gray) }
        }
    }

    /**
     * 底部加载更多正在加载中...
     * */
    @Composable
    fun LoadingItem() {
        Row(
            modifier = Modifier
                .height(34.dp)
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = Color.Gray,
                strokeWidth = 2.dp
            )
            Text(
                text = "加载中...",
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 20.dp),
                fontSize = 18.sp,
            )
        }
    }

    /**
     * 没有更多数据了
     * */
    @Composable
    fun NoMoreDataFindItem(onClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(
                onClick = { onClick() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(3.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                ),
            ) {
                Text(text = "已经没有更多数据啦 ~~ Click to top", color = Color.Gray)
            }
        }
    }
}