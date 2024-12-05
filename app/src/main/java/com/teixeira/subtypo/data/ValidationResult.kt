package com.teixeira.subtypo.data

sealed class ValidationResult {
  object Success : ValidationResult()

  class Error(val message: String) : ValidationResult()
}
