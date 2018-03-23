package repositories

import controllers.security.User
import org.scalatest.FlatSpec
import play.api.inject.guice.GuiceApplicationBuilder
import util.CommonSpec
import org.scalacheck.ScalacheckShapeless._

class UserRepositorySpec extends FlatSpec with CommonSpec {

  val userRepository: UserRepository = new GuiceApplicationBuilder().injector().instanceOf[UserRepository]

  it should "save and store user" in forAll() { rawUser: RawUser =>

    whenever(!rawUser.email.isEmpty) {

      val user: User = userRepository.create(rawUser.firstName, rawUser.lastName, rawUser.email, rawUser.password, rawUser.salt, rawUser.avatarURL).futureValue

      user.firstName shouldBe rawUser.firstName
      user.lastName shouldBe rawUser.lastName
      user.email shouldBe rawUser.email
      user.password shouldBe rawUser.password
      user.salt shouldBe rawUser.salt
      user.avatarURL shouldBe rawUser.avatarURL

      val result = userRepository.getUser(user.userID).futureValue

      result shouldBe Some(user)
    }
  }


  it should "find user by email" in forAll() { rawUser: RawUser =>

      whenever(!rawUser.email.isEmpty) {

        val user: User = userRepository.create(rawUser.firstName, rawUser.lastName, rawUser.email, rawUser.password, rawUser.salt, rawUser.avatarURL).futureValue

        user.firstName  shouldBe rawUser.firstName
        user.lastName  shouldBe rawUser.lastName
        user.email  shouldBe rawUser.email
        user.password  shouldBe rawUser.password
        user.salt  shouldBe rawUser.salt
        user.avatarURL shouldBe rawUser.avatarURL

        val userByEmail: Option[User] = userRepository.findByEmailAddress(user.email).futureValue

        userByEmail shouldBe Some(user)
      }
  }
}
