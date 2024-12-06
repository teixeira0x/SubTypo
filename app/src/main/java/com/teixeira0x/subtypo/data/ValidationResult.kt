package com.teixeira0x.subtypo.data

sealed class ValidationResult {
  object Success : ValidationResult()

  class Error(val message: String) : ValidationResult()
}
