package org.tn5250j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.connectdialog.ExternalProgram;
import org.tn5250j.interfaces.ConfigureFactory;

public class ExternalProgramConfig {

    protected static final String UNIX_SUFFIX = ".command.unix";
    protected static final String WINDOW_SUFFIX = ".command.window";
    protected static final String NAME_SUFFIX = ".command.name";
    protected static final String PREFIX = "etn.pgm.";

    private static Logger log = LoggerFactory.getLogger(ExternalProgramConfig.class);

	private static ExternalProgramConfig etnConfig;
	private static final String EXTERNAL_PROGRAM_REGISTRY_KEY = "etnPgmProps";
	private static final String EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME = "tn5250jExternalProgram.properties";
	private static final String EXTERNAL_PROGRAM_HEADER = "External Program Settings";

	protected final Map<String, String> etnPgmProps;
    private final List<ExternalProgram> programs = new LinkedList<>();

	public static ExternalProgramConfig getInstance(){
		if(etnConfig == null){
			etnConfig = new ExternalProgramConfig();
		}
		return etnConfig;
	}

	protected ExternalProgramConfig(){
		etnPgmProps = loadExternalProgramSettings();
		settingsToPrograms();
		sort();
	}

    protected Map<String, String> loadExternalProgramSettings() {
        Map<String, String> etnProps = new ConcurrentHashMap<>();
        try {
            etnProps = ConfigureFactory.getInstance().getProperties(EXTERNAL_PROGRAM_REGISTRY_KEY,
                    EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME, false, "Default Settings");

            log.info("begin loading external program settings");

            if (etnProps.isEmpty()) {
                final URL file = getClass().getClassLoader().getResource(EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME);
                final InputStream in = file.openStream();
                try {
                    etnProps = ConfigureFactory.loadProperties(in);
                } finally {
                    in.close();
                }

                ConfigureFactory.getInstance().saveSettings(EXTERNAL_PROGRAM_REGISTRY_KEY,
                        EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME, EXTERNAL_PROGRAM_HEADER);
            }

        } catch (final IOException ioe) {
            log.error(ioe.getMessage());
        } catch (final SecurityException se) {
            log.error(se.getMessage());
        }

        return etnProps;
    }

    /**
     * @param program external program
     */
    public synchronized void programUpdated(final ExternalProgram program) {
        final ExternalProgram existing = valueOf(program.getName());
        if (existing != null) {
            programs.remove(existing);
        }
        programs.add(program);
        sort();
        programsToProperties();
    }

    public synchronized void remove(final String name) {
        final ExternalProgram existing = valueOf(name);
        if (existing != null) {
            programs.remove(existing);
        }
        sort();
        programsToProperties();
    }

    /**
     * @param name program command name.
     * @return external program if found or null;
     */
    public synchronized ExternalProgram valueOf(final String name) {
        if (name == null) {
            return null;
        }

        for (final ExternalProgram p : programs) {
            if (name.equals(p.getName())) {
                return p;
            }
        }

        return null;
    }

    private void programsToProperties() {
        etnPgmProps.clear();

        int order = 0;
        for (final ExternalProgram p : programs) {
            etnPgmProps.put(PREFIX + order + NAME_SUFFIX, p.getName());
            etnPgmProps.put(PREFIX + order + WINDOW_SUFFIX, p.getWCommand());
            etnPgmProps.put(PREFIX + order + UNIX_SUFFIX, p.getUCommand());

            order++;
        }
    }

    private void settingsToPrograms() {
        final List<Integer> nums = getExternalProgramNumbers();
        nums.sort(Comparator.naturalOrder());

        for (final Integer num : nums) {
            final String program = etnPgmProps.get(PREFIX + num + NAME_SUFFIX);
            final String wCommand = etnPgmProps.get(PREFIX + num + WINDOW_SUFFIX);
            final String uCommand = etnPgmProps.get(PREFIX + num + UNIX_SUFFIX);
            programs.add(new ExternalProgram(program, wCommand, uCommand));
        }
    }

    private List<Integer> getExternalProgramNumbers() {
        final int offset = PREFIX.length();
        final int reminder = NAME_SUFFIX.length();
        return etnPgmProps.keySet().stream()
                .map(Object::toString)
                .filter(this::isCommandName)
                .map(k -> k.substring(offset, k.length() - reminder))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    private boolean isCommandName(final String k) {
        return k.startsWith(PREFIX) && k.endsWith(NAME_SUFFIX);
    }

    private void sort() {
        programs.sort(Comparator.naturalOrder());
    }

    public synchronized List<ExternalProgram> getPrograms() {
        return new LinkedList<>(programs);
    }

    public synchronized void save() {
        programsToProperties();
        ConfigureFactory.getInstance().saveSettings(
                EXTERNAL_PROGRAM_REGISTRY_KEY,
                EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME,
                EXTERNAL_PROGRAM_HEADER);
    }
}
