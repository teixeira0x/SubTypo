package com.teixeira0x.subtypo.ui.viewmodel.event

abstract class ViewEvent {

  data class Toast(val message: Int) : ViewEvent()
}
