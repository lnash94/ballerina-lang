package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenAPIFilter {
    public static List<OpenAPIPathSummary> filterOpenapi(OpenAPI openApi,
//                                                         ServiceNode serviceNode,
                                                         Filters filters
//                                                         Diagnostic.Kind kind
                                                         ) {
        boolean tagFilteringEnabled = filters.getTag().size() > 0;
        boolean operationFilteringEnabled = filters.getOperation().size() > 0;
        boolean excludeTagsFilteringEnabled = filters.getExcludeTag().size() > 0;
        boolean excludeOperationFilteringEnable = filters.getExcludeOperation().size() > 0;

        List<OpenAPIPathSummary> filteredOpenAliSummaries = new ArrayList<>();
        List<OpenAPIPathSummary> openAPIPathSummaries = MatchResourcewithOperationId.summarizeOpenAPI(openApi);


//        check based on the method and path filters
//        1. filter operation
//              1.1 filter priority
//                  * if tag filter enable -> out put  operation with dedicated tag
//                  * if operation filter enable -> out put operation with given name
//                  * if exclude tag filter enable -> out put all operation negation of tags
//                  * if exclude operation filter enable ->out put all operation expected that operation
//                  * if tag + operation -> out put operation with tag enable and given operation
//                  * if tag + exclude operation  -> out put operation that negation of every operation
//                  with tag and not including given operation
//                  * if operation + exclude tag -> all operations that not in exclude tags

        Iterator<OpenAPIPathSummary> openAPIIter = openAPIPathSummaries.iterator();
        while (openAPIIter.hasNext()) {
            OpenAPIPathSummary openAPIPathSummary = openAPIIter.next();
            // If operation filtering available proceed.
            // Else if proceed to check exclude operation filter is enable
            // Else check tag filtering or excludeTag filtering enable.
            if (operationFilteringEnabled) {
                // If tag filtering available validate only the filtered operations grouped by given tags.
                // Else if exclude tag filtering available validate only the operations that are not include
                // exclude Tags.
                // Else proceed only to validate filtered operations.
                if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (filters.getOperation().contains(operationMap.getValue().getOperationId())) {
//                                    check tag is available if it is null then remove other wise else-if not include
//                                    tag then remove operations.
                            if (operationMap.getValue().getTags() == null) {
                                operations.remove();
                            } else if (Collections.disjoint(filters.getTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                        operations.remove();
                            }
                        } else {
                            operations.remove();
                        }
                    }
                } else if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getOperation().contains(operationMap.getValue().getOperationId())) {
//                                    check tag is available
                            if (operationMap.getValue().getTags() != null) {
                                if (!Collections.disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                    operationIter.remove();
                                }
                            }
                        } else {
                            operationIter.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (!filters.getOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }
            } else if (excludeOperationFilteringEnable) {
                // If exclude tags filtering available validate only the filtered exclude operations grouped by
                // given exclude tags.
                // Else If tags filtering available validate only the operations that filtered by exclude
                // operations.
                // Else proceed only to validate filtered exclude operations.
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
//                                    check tag is available
                            if (operationMap.getValue().getTags() != null) {
                                if (!Collections.disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                    operationIter.remove();
                                }
                            } else {
                                operationIter.remove();
                            }
                        } else {
                            operationIter.remove();
                        }
                    }
                } else if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
//                                    check tag is available if it is null then remove other wise else-if not include
//                                    tag then remove operations.
                            if (operationMap.getValue().getTags() == null) {
                                operations.remove();
                            } else if (Collections.disjoint(filters.getTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                operations.remove();
                            }
                        } else {
                            operations.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }

                // If exclude tag filtering available proceed to validate all the operations grouped by tags which
                // are not included in list.
                // Else if validate the operations group by tag filtering
                // Else proceed without any filtering.
            } else {
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (operationMap.getValue().getTags() == null) {
                            break;
                        } else if (!Collections.disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                        operations.remove();
                        }
                    }


                } else if (tagFilteringEnabled) {
                    // If tag filtering available proceed to validate all the operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (operationMap.getValue().getTags() == null) {
                            operations.remove();
                        } else if (Collections.disjoint(filters.getTag(), operationMap.getValue().getTags())) {
//                                        remove operation
                                        operations.remove();
                        }
                    }
                }
            }
            if (openAPIPathSummary.getOperations().isEmpty()) {
                openAPIIter.remove();
            }
        }
        return openAPIPathSummaries;
    }

//        2. filter resources match operation
//        3. check extra resource
//        4. check extra operation
//        5. errors display in validator util instead of serviceValidator


}
