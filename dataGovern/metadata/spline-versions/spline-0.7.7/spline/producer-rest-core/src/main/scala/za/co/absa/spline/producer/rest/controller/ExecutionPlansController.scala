/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.producer.rest.controller

import java.util.UUID

import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation._
import za.co.absa.spline.producer.model.v1_1.ExecutionPlan
import za.co.absa.spline.producer.rest.ProducerAPI
import za.co.absa.spline.producer.service.repo.ExecutionProducerRepository

import scala.concurrent.{ExecutionContext, Future}

@RestController
@RequestMapping(consumes = Array(ProducerAPI.MimeTypeV1_1))
@Api(tags = Array("execution"))
class ExecutionPlansController @Autowired()(
  val repo: ExecutionProducerRepository) {

  import ExecutionContext.Implicits.global

  @PostMapping(Array("/execution-plans"))
  @ApiOperation(
    value = "Save Execution Plan",
    notes =
      """
        Saves an Execution Plan and returns its new UUID.

        Payload format:

        {
          // [Optional] Global unique identifier of the execution plan. If omitted, will be generated by the server.
          id: <UUID>,

          // [Optional] A name of the application (script, job etc) that this execution plan represents.
          name: <string>,

          // [Optional] A label that logically distinguish a group of one of multiple execution plans from another group.
          // If set, it has to match the discriminator of the associated execution events.
          // The property is used for UUID collision detection.
          discriminator: <string>,

          // Operation level lineage info
          operations: {

            // Write operation
            write: {
              // Operation ID (should be unique in the scope of the current execution plan)
              id: <string>,
              // [Optional] Operation name
              name: <string>,
              // Destination URI, where the data has been written to
              outputSource: <URI>,
              // Shows if the write operation appended or replaced the data in the target destination
              append: <boolean>,
              // Array of preceding operations IDs,
              // i.e. operations that serves as an input for the current operation
              childIds: [<string>],
              // [Optional] Operation parameters
              params: {...},
              // [Optional] Custom info about the operation
              extra: {...}
            },

            // Array of read operations
            reads: [
              {
                // Operation ID (see above)
                id: <string>,
                // [Optional] Operation name
                name: <string>,
                // Source URIs, where the data has been read from
                inputSources: [<URI>],
                // [Optional] Output attribute IDs
                output: [<string>],
                // [Optional] Operation parameters
                params: {...},
                // [Optional] Custom info about the operation
                extra: {...}
              },
              ...
            ],

            // Other operations
            other: [
              {
                // Operation ID (see above)
                id: <string>,
                // [Optional] Operation name
                name: <string>,
                // Array of preceding operations IDs (see above)
                childIds: [<string>],
                // [Optional] Output attribute IDs. If output is undefined the server will try to infer it from the child operations' output.
                output: [<string>],
                // [Optional] Operation parameters
                params: {...},
                // [Optional] Custom info about the operation
                extra: {...}
              },
              ...
            ]
          },

          // [Optional] Attribute definitions
          attributes: [
            {
              id: <string>,
              // Attribute name
              name: <string>,
              // [Optional] References to other attributes, expressions or constants that this attribute is computed from
              childRefs: [ { __exprId: <string> } | { __attrId: <string> } ],
              // [Optional] Custom info
              extra: {...}
            }
          ],

          // [Optional] Attribute level lineage info
          expressions: {

            // Function definitions
            functions: [
              {
                id: <string>,
                // Function name
                name: <string>,
                // [Optional] References to operands (expressions, constants or attributes)
                childRefs: [ { __exprId: <string> } | { __attrId: <string> } ],
                // [Optional] Named expression parameters
                params: {...},
                // [Optional] Custom meta info
                extra: {...}
              }
            ],

            // Constant/Literal definitions
            constants: {
              {
                id: <string>,
                // constant value
                value: <any>,
                // [Optional] Custom meta info
                extra: {...}
              }
            }
          },

          // Information about the data framework in use (e.g. Spark, StreamSets etc)
          systemInfo: {
            name: <string>,
            version: <string>
          },

          // [Optional] Spline agent information
          agentInfo: {
            name: <string>,
            version: <string>
          },

          // [Optional] Any other extra info associated with the current execution plan
          extraInfo: {...}
        }
      """)
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Execution Plan is stored with the UUID returned in a response body")
  ))
  @ResponseStatus(HttpStatus.CREATED)
  def executionPlan(@RequestBody execPlan: ExecutionPlan): Future[UUID] = repo
    .insertExecutionPlan(execPlan)
    .map(_ => execPlan.id)

}