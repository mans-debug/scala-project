package org.itis.mansur
package scalagram.models.security

import java.util.UUID

case class Password(login: String, password: String)

case class AccessToken(token: UUID)
