/**
 * The original code ran long jobs (loading, building the surface) in separate
 * Java threads. In the browser they run as async functions on the main thread
 * and call Thread.yield() regularly so that progress dialogs can repaint.
 */
export const Thread = {
    yield() {
        return new Promise((resolve) => {
            const channel = new MessageChannel();
            channel.port1.onmessage = () => resolve();
            channel.port2.postMessage(null);
        });
    },

    sleep(ms) {
        return new Promise((resolve) => setTimeout(resolve, ms));
    },
};
