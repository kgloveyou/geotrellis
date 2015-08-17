/*
* Copyright (c) 2015 Azavea.
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

package geotrellis.vector.interpolation

import geotrellis.vector.{PointFeature, Point}

/** Methods implicitly added to Array[Point] via the package import.
  * Contains a method for each overloaded way to create a GeoKriging
  */
trait GeoKrigingVectorMethods {
  val pointArr: Array[Point]

  def geoKriging(points: Array[PointFeature[Double]]) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points))

  def geoKriging(points: Array[PointFeature[Double]], bandwidth: Double) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, bandwidth))

  def geoKriging(points: Array[PointFeature[Double]], model: ModelType) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, model))

  def geoKriging(points: Array[PointFeature[Double]], bandwidth: Double, model: ModelType) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, bandwidth, model))

  def geoKriging(points: Array[PointFeature[Double]], attrFunc: (Double, Double) => Array[Double]) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, attrFunc))

  def geoKriging(points: Array[PointFeature[Double]], attrFunc: (Double, Double) => Array[Double], bandwidth: Double) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, attrFunc, bandwidth))

  def geoKriging(points: Array[PointFeature[Double]], attrFunc: (Double, Double) => Array[Double], model: ModelType) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, attrFunc, model))

  def geoKriging(points: Array[PointFeature[Double]], attrFunc: (Double, Double) => Array[Double], bandwidth: Double, model: ModelType) =
    InterpolationVector.kriging(pointArr)(GeoKriging(points, attrFunc, bandwidth, model))
}
