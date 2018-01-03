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
package straightway.units

/**
 * Base class for quantities simplifying the implementation in common cases.
 */
abstract class QuantityBase(
    private val symbol: String,
    final override val scale: UnitScale,
    override val baseMagnitude: Number,
    private val scaler: (UnitScale) -> QuantityBase)
    : Quantity
{
    constructor(symbol: String, scale: UnitScale, scaler: (UnitScale) -> QuantityBase)
        : this(symbol, scale, 1, scaler)

    override val id: QuantityId by lazy { "${this::class.hashCode()}" }
    override fun withScale(scale: UnitScale) = scaler(scale)
    override fun toString() = "$scale$symbol"
    override fun equals(other: Any?) =
        other != null &&
            this::class == other::class &&
            other is QuantityBase &&
            id == other.id &&
            scale == other.scale

    override fun hashCode() = id.hashCode() xor scale.hashCode()
}