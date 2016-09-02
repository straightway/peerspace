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

package ui

import (
	"github.com/andlabs/ui"
)

type MainWindow struct {
	*ui.Window
}

func NewMainWindow() *MainWindow {
	mainWindow := &MainWindow{}
	mainWindow.Window = ui.NewWindow("Straightway Simulation", 200, 100, false)

	mainLayout := ui.NewVerticalBox()

	commandBar := ui.NewHorizontalBox()
	mainLayout.Append(commandBar, false)

	stopButton := ui.NewButton("#")
	stopButton.Disable()
	commandBar.Append(stopButton, false)

	pauseButton := ui.NewButton("||")
	pauseButton.Disable()
	commandBar.Append(pauseButton, false)

	startButton := ui.NewButton(">")
	commandBar.Append(startButton, false)

	stretcher := ui.NewVerticalBox()
	commandBar.Append(stretcher, true)

	labelVCenter := NewVCenteredLabel("01.01.0000 00:00:00.000")
	commandBar.Append(labelVCenter, false)

	mainArea := ui.NewHorizontalBox()
	mainLayout.Append(mainArea, true)

	mainWindow.SetChild(mainLayout)
	mainWindow.OnClosing(func(*ui.Window) bool {
		ui.Quit()
		return true
	})

	return mainWindow
}
