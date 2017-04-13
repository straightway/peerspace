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
package straightway.general.dsl

fun <TArg, TResult> untyped(typed: (TArg) -> TResult): (Any) -> Any
    = { typed(it as TArg) as Any }

fun <T> untypedOp(typed: (T) -> T): (Any) -> Any
    = { typed(it as T) as Any }

fun <TArg1, TArg2, TResult> untyped(typed: (TArg1, TArg2) -> TResult): (Any, Any) -> Any
    = { a, b -> typed(a as TArg1, b as TArg2) as Any }

fun <T> untypedOp(typed: (T, T) -> T): (Any, Any) -> Any
    = { a, b -> typed(a as T, b as T) as Any }
