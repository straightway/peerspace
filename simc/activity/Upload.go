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

package activity

import (
	"time"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/sim/randvar"
)

type Upload struct {
	User               sim.User
	Configuration      *app.Configuration
	Delay              randvar.Duration
	DataSize           randvar.Float64
	IdGenerator        id.Generator
	ChunkCreator       sim.ChunkCreator
	AudienceProvider   sim.AudienceProvider
	AttractionRatio    randvar.Float64
	AudiencePermutator randvar.Permutator
}

func (this *Upload) ScheduleUntil(maxTime time.Time) {
	scheduler := this.User.Scheduler()
	now := scheduler.Time()
	nextActionTime := now.Add(this.Delay.NextSample())
	for !maxTime.Before(nextActionTime) {
		scheduler.ScheduleAbsolute(nextActionTime, this.doPush)
		nextActionTime = nextActionTime.Add(this.Delay.NextSample())
	}
}

// Private

func (this *Upload) doPush() {
	newDataChunk := this.ChunkCreator.CreateChunk(
		data.Key{Id: this.IdGenerator.NextId()},
		this.nextChunkSize())
	this.User.Node().Push(newDataChunk, this.User)
	this.attractToAudience(newDataChunk)
}

func (this *Upload) nextChunkSize() uint64 {
	chunkSize := uint64(this.DataSize.NextSample())
	if this.Configuration.MaxChunkSize < chunkSize {
		chunkSize = this.Configuration.MaxChunkSize
	} else if chunkSize <= 0 {
		chunkSize = 1
	}

	return chunkSize
}

func (this *Upload) attractToAudience(chunk *data.Chunk) {
	audience := this.AudienceProvider.Audience()

	attractionRatio := this.AttractionRatio.NextSample()
	audienceCount := len(audience)
	numberOfAttractions := int(float64(audienceCount) * attractionRatio)
	if numberOfAttractions < 0 {
		numberOfAttractions = 0
	} else if audienceCount < numberOfAttractions {
		numberOfAttractions = audienceCount
	}

	permutatedAudience := make([]sim.DataConsumer, audienceCount, audienceCount)
	for i, j := range this.AudiencePermutator.Perm(audienceCount) {
		permutatedAudience[i] = audience[j]
	}

	chunkQuery := data.Query{Id: chunk.Key.Id}
	for _, consumer := range permutatedAudience[0:numberOfAttractions] {
		consumer.AttractTo(chunkQuery)
	}
}
