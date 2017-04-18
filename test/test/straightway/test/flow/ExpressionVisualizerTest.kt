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
package straightway.test.flow

import org.junit.jupiter.api.Test
import straightway.general.dsl.BoundExpr
import straightway.general.dsl.Expr
import straightway.general.dsl.FunExpr
import straightway.general.dsl.Value

class ExpressionVisualizerTest {
    @Test fun value_visualizedDirectly() {
        val testedExpr = Value("Hello")
        expect(ExpressionVisualizer(testedExpr).string _is equal to "Hello")
    }

    @Test fun monadicOp_visualizedFunctionCallStyle() {
        val testedExpr = func(1)-"arg"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "func1(arg)")
    }

    @Test fun dyadicOp_visualizedOperatorStyle() {
        val testedExpr = func(2)-"arg1"-"arg2"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "arg1 func2 arg2")
    }

    @Test fun arity3Op_visualizedFunctionCallStyle() {
        val testedExpr = func(3)-"arg1"-"arg2"-"arg3"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "func3(arg1, arg2, arg3)")
    }

    @Test fun combinedMonadicDyadicOp() {
        val testedExpr = func(1)-func(2)-"arg1"-"arg2"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "arg1 func1-func2 arg2")
    }

    @Test fun combinedDyadicMonadicOp() {
        val testedExpr = func(2)-func(1)-"arg1"-"arg2"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "func1(arg1) func2 arg2")
    }

    @Test fun deeplyNested() {
        val testedExpr = func(3, "top")-func(2, "sub1")-"arg1"-func(1, "op1")-func(1, "op2")-"arg2"-"arg3"-func(2, "sub2")-"arg4"-"arg5"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "top(arg1 sub1 op1-op2(arg2), arg3, arg4 sub2 arg5)")
    }

    @Test fun missingArgumentsAreFilled() {
        val testedExpr = func(2)-"arg1"
        expect(ExpressionVisualizer(testedExpr).string _is equal to "arg1 func2 ?")
    }

    private companion object {
        operator fun Expr.minus(e: Expr) = BoundExpr(this, e)
        operator fun Expr.minus(v: Any) = BoundExpr(this, Value(v))
        fun func(arity: Int) = func(arity, "func$arity")
        fun func(arity: Int, name: String) = FunExpr(arity, name, { _ -> "result of $name" })
    }
}