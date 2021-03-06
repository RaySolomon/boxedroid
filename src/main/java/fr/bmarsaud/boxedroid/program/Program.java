package fr.bmarsaud.boxedroid.program;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import fr.bmarsaud.boxedroid.entity.exception.SDKException;
import fr.bmarsaud.boxedroid.program.observer.ErrorObserver;
import fr.bmarsaud.boxedroid.program.observer.InfoObserver;

/**
 * A abstraction of an external executable program
 */
public class Program extends Observable {
    private String path;
    private Map<String, String> environment;
    private Process process;
    private SDKException exception;

    private ArrayList<Observer> infoObservers;
    private ArrayList<Observer> errorObservers;

    private InfoObserver infoObserver;
    private ErrorObserver errorObserver;

    public Program(String path, Map<String, String> environment) {
        this.path = path;
        this.environment = environment;
        this.infoObservers = new ArrayList<>();
        this.errorObservers = new ArrayList<>();
        this.infoObserver = new InfoObserver();
        this.errorObserver = new ErrorObserver();
    }

    public Program(String path) {
        this.path = path;
        this.environment = new HashMap<>();
        this.infoObservers = new ArrayList<>();
        this.errorObservers = new ArrayList<>();
        this.infoObserver = new InfoObserver();
        this.errorObserver = new ErrorObserver();
    }

    /**
     * Execute the program with the given arguments
     * @param args The arguments passed to the program
     * @return The process of the executed program
     */
    public Process execute(String... args) throws IOException {
        List<String> command = new ArrayList<>(Arrays.asList(args));
        command.add(0, path);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(environment);

        process = pb.start();

        listenInfoStream();
        listenErrorStream();

        return process;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public SDKException getException() {
        return exception;
    }

    public void setException(SDKException exception) {
        this.exception = exception;
    }

    /**
     * Add an environment variable to the next execution of the program
     * @param key The key of the environment variable
     * @param value The value of the environment variable
     * @return The updated program
     */
    public Program addEnvironment(String key, String value) {
        environment.put(key, value);
        return this;
    }

    /**
     * Register an observer to the future executions of the program info stream
     * @param observer The observer to register
     */
    public void onInfo(Observer observer) {
        infoObservers.add(observer);
    }

    /**
     * Unregister an observer of the future executions of the program info stream
     * @param observer The observer to unregister
     */
    public void unInfo(Observer observer) {
        infoObservers.remove(observer);
    }

    /**
     * Register an observer to the future executions of the program error stream
     * @param observer The observer to register
     */
    public void onError(Observer observer) {
        errorObservers.add(observer);
    }

    /**
     * Unregister an observer of the future executions of the program error stream
     * @param observer The observer to unregister
     */
    public void unError(Observer observer) {
        errorObservers.remove(observer);
    }

    /**
     * Listen to the current process info input stream and call all registered observers for
     * each line received
     */
    private void listenInfoStream() {
        observersListenToStream(infoObservers, process.getInputStream());
    }

    /**
     * Listen to the current process error input stream and call all registered observers for
     * each line received
     */
    private void listenErrorStream() {
        observersListenToStream(errorObservers, process.getErrorStream());
    }

    /**
     * Make a list of observers listen to a input steam. The observers are called for each line
     * received
     * @param observers The observers list that will listen
     * @param stream The stream to listen to
     */
    private void observersListenToStream(ArrayList<Observer> observers, InputStream stream) {
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            try {
                while ((line = reader.readLine()) != null) {
                    for(Observer observer : observers) {
                        observer.update(this, line);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Send an input string to the current process output stream
     * @param input The string to send to the process
     * @throws IOException
     */
    public void sendInput(String input) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(input, 0, input.length());
        writer.newLine();
        writer.close();
    }


    /**
     * Activate or deactivate the logging of the program output to the standard output
     * @param redirect True to activate the redirection, False instead
     */
    public void redirectInfo(boolean redirect) {
        if(redirect) {
            onInfo(infoObserver);
        } else {
            unInfo(infoObserver);
        }
    }

    /**
     * Activate or deactivate the logging of the program error to the standard error
     * @param redirect True to activate the redirection, False instead
     */
    public void redirectError(boolean redirect) {
        if(redirect) {
            onError(errorObserver);
        } else {
            unError(errorObserver);
        }
    }
}
