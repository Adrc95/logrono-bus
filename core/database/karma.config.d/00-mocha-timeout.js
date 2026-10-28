// Karma's mocha adapter defaults to 2 s per test, and the Room test on the browser waits on a
// worker round trip plus a cold OPFS database: it was failing with "Timeout of 2000ms exceeded",
// not with an assertion. 30 s is room for a slow first open without hiding a real hang, because
// Karma still fails the run if the promise never settles.
config.set({
    client: {
        mocha: {
            timeout: 30000
        }
    }
});
