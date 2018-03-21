package util

import org.scalatest.Matchers
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.prop.PropertyChecks

trait CommonSpec extends Matchers with PropertyChecks with ScalaFutures with Eventually
