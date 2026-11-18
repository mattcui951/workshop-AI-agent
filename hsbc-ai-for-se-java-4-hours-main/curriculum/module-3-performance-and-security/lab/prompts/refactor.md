# Prompt - refactor with a safety net

The attached TradeReconciler works but is slow and verbose. Refactor it WITHOUT changing behaviour:

1. findDuplicateTradeIds is O(n^2). Make it a single pass. Keep the exact same output, including the ORDER of the ids.
2. notionalByMetal and countByStatus can use Streams/Collectors if that makes them clearer.

Rules: do not edit anything in src/test. After you propose the change, list the behaviours you think could differ
(ordering, null handling, floating-point sums) so I know what to test.
