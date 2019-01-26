/*
 * Copyright 2016 github.com/straightway
 *
 *  Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package straightway.peerspace.net

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Same
import straightway.testing.flow.True
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.getHandlers
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

class RequestTest {

    private companion object {
        val originatorId = Id("originator")
        val chunk = DataChunk(Key(Id("chunk")), byteArrayOf())
        val chunkRequestType = Request::class.createType(
                arguments = listOf(KTypeProjection(
                        KVariance.INVARIANT,
                        DataChunk::class.starProjectedType)))
    }

    @Test
    fun `typeSelector matches proper type`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                typeSelector
            } then {
                expect(typeSelector(chunkRequestType) is_ True)
            }

    @Test
    fun `typeSelector does not match other type`() =
            Given {
                Request(originatorId, DataChunkQuery(Id("queriedId")))
            } when_ {
                typeSelector
            } then {
                expect(typeSelector(chunkRequestType) is_ False)
            }

    @Test
    fun `content is accessible`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                content
            } then {
                expect(it.result is_ Same as_ chunk)
            }

    @Test
    fun `originatorId is accessible`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                content
            } then {
                expect(it.result is_ Same as_ chunk)
            }

    @Test
    fun `typeSelector of createDynamically matches proper type`() =
            Given {
                Request.createDynamically(originatorId, chunk)
            } when_ {
                typeSelector
            } then {
                expect(typeSelector(chunkRequestType) is_ True)
            }

    @Test
    fun `typeSelector of createDynamically does not match other type`() =
            Given {
                Request.createDynamically(originatorId, DataChunkQuery(Id("query")))
            } when_ {
                typeSelector
            } then {
                expect(typeSelector(chunkRequestType) is_ False)
            }

    @Test
    fun `createDynamically result matches according handler function`() {
        val handler = mock<SingleHandler>()
        Given {
            Request.createDynamically(originatorId, chunk)
        } when_ {
            handler.getHandlers<RequestHandler>(typeSelector).single()(this)
        } then {
            @Suppress("UNCHECKED_CAST")
            verify(handler).pushDataChunk(this as Request<DataChunk>)
        }
    }

    @Test
    fun `handle calls matching handler function`() {
        val handler = mock<SingleHandler>()
        Given {
            Request.createDynamically(originatorId, chunk)
        } when_ {
            handler handle this
        } then {
            @Suppress("UNCHECKED_CAST")
            verify(handler).pushDataChunk(this as Request<DataChunk>)
        }
    }

    @Test
    fun `handle calls all matching handler function2`() {
        val handler = mock<TwoHandlers>()
        Given {
            Request.createDynamically(originatorId, chunk)
        } when_ {
            handler handle this
        } then {
            @Suppress("UNCHECKED_CAST")
            val request = this as Request<DataChunk>
            verify(handler).pushDataChunk1(request)
            verify(handler).pushDataChunk2(request)
        }
    }

    @Test
    fun `handle does not call not matching handler function`() {
        val handler = mock<SingleHandler>()
        Given {
            Request.createDynamically(originatorId, chunk)
        } when_ {
            handler handle this
        } then {
            @Suppress("UNCHECKED_CAST")
            verify(handler, never()).queryData(any())
        }
    }

    @Test
    fun `toString yields expected result`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "Request(${remotePeerId.identifier} -> $chunk)")
            }

    @Test
    fun `two Requests with the same originator id and content are equal`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                equals(Request(remotePeerId, chunk))
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `Requests are equal regardless of creation method`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                equals(Request.createDynamically(remotePeerId, chunk))
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `a Request is not equal to another type's instance`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                Request(remotePeerId, chunk).equals(83)
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `two Requests with the different originator ids are not equal`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                equals(Request(Id("otherOriginator"), chunk))
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `two Requests with the different contents are not equal`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                equals(Request(remotePeerId, DataChunk(Key(Id("otherChunk")), byteArrayOf())))
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `hashCode is computed from originatorId and content`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                hashCode()
            } then {
                expect(it.result is_ Equal to_ (remotePeerId.hashCode() xor chunk.hashCode()))
            }

    @Test
    fun `id returns id of content`() =
            Given {
                Request(originatorId, chunk)
            } when_ {
                id
            } then {
                expect(it.result is_ Equal to_ chunk.id)
            }

    private interface SingleHandler {
        @RequestHandler
        fun pushDataChunk(req: Request<DataChunk>)

        @RequestHandler
        fun queryData(req: Request<DataChunkQuery>)
    }

    private interface TwoHandlers {
        @RequestHandler
        fun pushDataChunk1(req: Request<DataChunk>)
        @RequestHandler
        fun pushDataChunk2(req: Request<DataChunk>)
    }
}