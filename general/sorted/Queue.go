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

package sorted

type Queue struct {
	root *node
}

type node struct {
	less           *node
	item           Item
	greaterOrEqual *node
}

func (this *node) insert(item Item) {
	switch {
	case item.IsLessThan(this.item):
		if this.less == nil {
			this.less = &node{item: item}
		} else {
			this.less.insert(item)
		}
	default:
		if this.greaterOrEqual == nil {
			this.greaterOrEqual = &node{item: item}
		} else {
			this.greaterOrEqual.insert(item)
		}
	}
}

func (this *node) pop() (result Item, newRoot *node) {
	if this.less != nil {
		result, this.less = this.less.pop()
		newRoot = this
	} else {
		result = this.item
		newRoot = this.greaterOrEqual
	}

	return
}

func NewQueue() *Queue {
	return &Queue{}
}

func (this *Queue) Pop() Item {
	if this.root == nil {
		return nil
	}

	var result Item
	result, this.root = this.root.pop()
	return result
}

func (this *Queue) Insert(item Item) {
	if this.root == nil {
		this.root = &node{item: item}
		return
	}

	this.root.insert(item)
}
