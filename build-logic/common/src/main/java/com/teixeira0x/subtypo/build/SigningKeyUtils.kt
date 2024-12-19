/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.build

import java.util.Base64
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

private const val KEY_BASE64 = "SIGNING_KEY_BASE64"
private const val KEY_PASSWORD = "SIGNING_KEY_PASSWORD"
private const val KEY_ALIAS = "SIGNING_KEY_ALIAS"

val Project.signingKeyFile: Provider<RegularFile>
  get() = rootProject.layout.buildDirectory.file("signing-key.jks")

object SigningKeyUtils {

  fun Project.writeSigningKey() {
    val signingKey = signingKeyFile.get().asFile
    if (signingKey.exists()) {
      return
    }
    signingKey.parentFile?.mkdirs()

    getEnvOrProp(key = KEY_BASE64)?.also { bin ->
      signingKey.writeBytes(Base64.getDecoder().decode(bin))
    }
  }

  fun Project.getSigningKeyAlias(): String? {
    return getEnvOrProp(KEY_ALIAS)
  }

  fun Project.getSigningKeyPass(): String? {
    return getEnvOrProp(KEY_PASSWORD)
  }

  private fun Project.getEnvOrProp(key: String): String? {
    var value: String? = System.getenv(key)
    if (value.isNullOrBlank()) {
      value = project.properties[key] as? String?
    }
    return value
  }
}
