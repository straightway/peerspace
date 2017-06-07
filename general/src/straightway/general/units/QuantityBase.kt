/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.general.units

abstract class QuantityBase(
    private val shortIdBase: String,
    final override val scale: UnitScale,
    private val scaler: (UnitScale) -> QuantityBase)
    : Quantity
{
    override val shortId by lazy { "$siScaleCorrection$shortIdBase" }
    override fun withScale(scale: UnitScale) = scaler(scale)
    override fun toString() = "$scale$shortIdBase"
    override fun equals(other: Any?) =
        other != null &&
        this::class == other::class &&
        other is QuantityBase &&
        shortId == other.shortId &&
        scale == other.scale
    override fun hashCode() = shortId.hashCode() xor scale.hashCode()
}