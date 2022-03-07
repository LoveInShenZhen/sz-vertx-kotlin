# GRPC Connection Backoff Protocol
When we do a connection to a backend which fails, it is typically desirable to not retry immediately (to avoid flooding the network or the server with requests) and instead do some form of exponential backoff.

We have several parameters:

- ==INITIAL_BACKOFF== (how long to wait after the first failure before retrying)
- MULTIPLIER (factor with which to multiply backoff after a failed retry)
- JITTER (by how much to randomize backoffs).
- MAX_BACKOFF (upper bound on backoff)
- MIN_CONNECT_TIMEOUT (minimum time we're willing to give a connection to complete)
