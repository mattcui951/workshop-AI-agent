"""Run AFTER your refactor to see WHERE the speed-up came from:   python3 benchmark_demo.py
Rough stopwatch timings (best of 5): good enough to see an order of magnitude, not a rigorous benchmark."""
import sys
import time

sys.path.insert(0, "tests")
import legacy_reconciler as legacy   # noqa: E402
import reconciler as yours           # noqa: E402
from test_oracle import random_transactions   # noqa: E402


def ms(fn, runs=5):
    best = float("inf")
    for _ in range(runs):
        start = time.perf_counter()
        fn()
        best = min(best, time.perf_counter() - start)
    return best * 1000


small = random_transactions(5000, 4700, 42)
big = random_transactions(400_000, 10_000_000, 43)
print(f"find_duplicate_ids (5,000 transactions):   original {ms(lambda: legacy.find_duplicate_ids(small)):9.1f} ms | yours {ms(lambda: yours.find_duplicate_ids(small)):9.1f} ms")
print(f"total_by_currency  (400,000 transactions): original {ms(lambda: legacy.total_by_currency(big)):9.1f} ms | yours {ms(lambda: yours.total_by_currency(big)):9.1f} ms")
print(f"count_by_status    (400,000 transactions): original {ms(lambda: legacy.count_by_status(big)):9.1f} ms | yours {ms(lambda: yours.count_by_status(big)):9.1f} ms")
