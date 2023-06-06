package org.itis.mansur
package scalagram.models.security

class SecurityError(val message: String) extends RuntimeException(message)

class PasswordCreationNotSupported extends SecurityError("you trying to create password")
class CredNotValid extends SecurityError("bad creds")
class UserNotFound extends SecurityError("no such user")
