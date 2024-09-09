package com.teixeira.subtitles.data

sealed class ValidationResult {
  object Success : ValidationResult()

  class Error(val message: String) : ValidationResult()
}
