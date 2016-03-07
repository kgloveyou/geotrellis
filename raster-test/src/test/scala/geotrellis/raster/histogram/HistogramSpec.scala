/*
 * Copyright (c) 2014 Azavea.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.raster.histogram

import Console.printf
import math.round
import org.scalatest._

import scala.util.Random
import java.util.Locale


class HistogramSpec extends FunSpec with Matchers {
  def stringToInts(s:String) = {
    s.toCharArray.map { _.toByte - 32 }
  }
  val kinds = List(("ArrayHistogram", ArrayHistogram, () => {ArrayHistogram(100)}),
                   ("FastMapHistogram", FastMapHistogram, () => {FastMapHistogram()}))
  kinds.foreach {
    case (name, cls, builder) => {
      describe("A " + name) {
        it("should build") {
          builder()
        }

        it("should know its min and max values") {
          val h = builder()

          h.countItem(4)
          h.countItem(13)
          h.countItem(84)

          h.getMinValue should be (4)
          h.getMaxValue should be (84)
        }

        // REFACTOR: this should use option
        it("should behave predictably when empty") {
          val h = builder()
          // min value should be largest possible int
          // max value should be smallest possible int
          // this way it signals that the values don't really make sense
          h.getMinValue should be (Int.MaxValue)
          h.getMaxValue should be (Int.MinValue)
        }

        it("should store values and retrieve them later") {
          val h = builder()

          val s = "This is some great test data--see?"
          stringToInts(s).foreach { i => h.countItem(i) }

          h.getItemCount('z'.toByte - 32) should be (0)
          h.getItemCount('?'.toByte - 32) should be (1)
          h.getItemCount('T'.toByte - 32) should be (1)
          h.getItemCount('i'.toByte - 32) should be (2)
          h.getItemCount('s'.toByte - 32) should be (5)
          h.getItemCount(' '.toByte - 32) should be (5)
        }

        it("should do fancy kinds of counting and uncounting") {
          val h = builder()
          h.countItem(6, 30)
          h.countItem(8, 12)
          h.countItem(16, 20)
          h.uncountItem(16)

          h.getTotalCount should be (42)
          h.getMinValue should be (6)
          h.getMaxValue should be (8)
        }

        it("should generate quantile breaks") {
          val h = builder()

          val s = "weio gwej ah;iodfbo;dzfb;oaerge4;oi 34ch4oinj;;bjsdon;bsd; " +
          "jioijo43hi4oiernhow4y734herojniejnibodf[h0shemjhermjbdfmb j;jgj;gseg" +
          "43wjtnejinherhoe9980437843t43n8hy8h89huntjhgfjogfdtgj895n34y8nt34tpn"
          stringToInts(s).foreach { i => h.countItem(i) }

          h.getQuantileBreaks(6) should be (Array(23, 67, 71, 73, 78, 90))

          //println(h.generateStatistics)
        }

        it("should handle quantile breaks with extreme values") {
          val h = ArrayHistogram(10)
          h.countItem(1, 10)
          h.countItem(2, 1000)
          h.countItem(3, 120)
          h.countItem(4, 100)
          h.countItem(5, 80)
          h.countItem(6, 20)

          h.getQuantileBreaks(3) should be (Array(2, 3, 6))
        }

        it("should handle quantile breaks with multiple extreme values") {
          val h = ArrayHistogram(10)
          h.countItem(1, 20)
          h.countItem(2, 60)
          h.countItem(3, 10000)
          h.countItem(4, 10)
          h.countItem(5, 10000)
          h.countItem(6, 70)
          h.countItem(7, 50)

          h.getQuantileBreaks(5) should be (Array(2, 3, 5, 6, 7))
        }
      }
    }
  }

  describe("A severely unbalanced histogram") {
    it("should handle a tricky double unbalanced starting values") {
      val h = ArrayHistogram(10)
      h.countItem(0, 100)
      h.countItem(1, 1000)
      h.countItem(2, 15)
      h.countItem(3, 10)
      h.countItem(4, 5)
      h.countItem(5, 10)
      h.getQuantileBreaks(4) should be (Array(0, 1, 2, 5))
    }

    it("should handle a tricky double unbalanced later values") {
      val h = ArrayHistogram(10)
      h.countItem(0, 10)
      h.countItem(1, 100)
      h.countItem(2, 1000)
      h.countItem(3, 15)
      h.countItem(4, 10)
      h.countItem(5, 5)
      h.getQuantileBreaks(4) should be (Array(0, 1, 2, 5))
    }
  }

  describe("A Histogram") {
    it("should be able to handle any number of quantiles") {
      val h = ArrayHistogram(10)
      h.countItem(1, 10)
      h.countItem(2, 10)
      h.countItem(3, 10)
      h.countItem(4, 10)
      h.countItem(5, 10)

      h.getQuantileBreaks(1) should be (Array(5))
      h.getQuantileBreaks(2) should be (Array(2,5))
      h.getQuantileBreaks(3) should be (Array(2,3,5))
      h.getQuantileBreaks(4) should be (Array(1,2,4,5))
      h.getQuantileBreaks(5) should be (Array(1,2,3,4,5))
      h.getQuantileBreaks(6) should be (Array(1,2,3,4,5))
      h.getQuantileBreaks(7) should be (Array(1,2,3,4,5))
      h.getQuantileBreaks(8) should be (Array(1,2,3,4,5))
      h.getQuantileBreaks(9) should be (Array(1,2,3,4,5))
      h.getQuantileBreaks(10) should be (Array(1,2,3,4,5))
    }
  }

  describe("The statistics generator") {
    it("should generate stats where there are zero values") {
      val h = ArrayHistogram(20)
      h.countItem(0, 0)
      h.countItem(1, 0)
      h.countItem(2, 10)
      h.countItem(3, 15)
      h.countItem(4, 0)
      h.countItem(5, 18)
      h.countItem(6, 9)
      h.countItem(7, 2)
      h.countItem(8, 0)
      h.countItem(9, 0)

      val stats = h.generateStatistics
      //println(stats)
      "%.3f".formatLocal(Locale.ENGLISH, stats.mean) should be ("4.130")
      stats.median should be (5)
      stats.mode should be (5)
      "%.3f".formatLocal(Locale.ENGLISH, stats.stddev) should be ("1.528")
      stats.zmin should be (2)
      stats.zmax should be (7)
    }
  }

}
