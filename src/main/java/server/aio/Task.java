package server.aio;

interface Task {
    void execute(int threadId);
}
