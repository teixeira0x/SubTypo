package com.teixeira0x.subtypo.data

sealed class ValidationResult {
  object Success : com.teixeira0x.subtypo.data.ValidationResult()

  class Error(val message: String) : com.teixeira0x.subtypo.data.ValidationResult()
}
