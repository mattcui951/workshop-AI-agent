"""Starts RED on purpose: until you fix the algorithm, your code is exactly as slow as the original.
A rough stopwatch check, not a rigorous benchmark."""
import time

import legacy_reconciler as legacy
import reconciler as yours
from test_oracle import random_transactions


def best_of(runs, fn):
    best = float("inf")
    for _ in range(runs):
        start = time.perf_counter()
        fn()
        best = min(best, time.perf_counter() - start)
    return best


def test_duplicate_detection_is_at_least_three_times_faster_than_the_original():
    data = random_transactions(5000, 4700, 42)
    legacy_s = best_of(3, lambda: legacy.find_duplicate_ids(data))
    yours_s = best_of(3, lambda: yours.find_duplicate_ids(data))
    assert yours_s * 3 < legacy_s, f"Not fast enough yet: original={legacy_s*1000:.1f} ms, yours={yours_s*1000:.1f} ms"
