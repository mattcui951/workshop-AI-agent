# Prompt: refactor with a safety net

The attached reconciler.py works but is slow and verbose. Refactor it WITHOUT changing behaviour.

1. find_duplicate_ids is O(n^2). Make it a single pass. Keep the exact same output, including the ORDER of the ids
   (first occurrence first).
2. total_by_currency and count_by_status can use collections (defaultdict, Counter) if that makes them clearer.

Rules: do not edit anything in tests/. After you propose the change, list the behaviours you think could differ
(ordering, empty input, number types) so I know what to test.
