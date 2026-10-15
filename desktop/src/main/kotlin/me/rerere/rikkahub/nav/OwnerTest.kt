package me.rerere.rikkahub.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun testOwner() = LocalViewModelStoreOwner.current
