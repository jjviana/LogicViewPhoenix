import { Logger, Level, System } from '../../../../compat/util/Logger.js';
import { ApplicationRepository } from './ApplicationRepository.js';

const log = Logger.getLogger('mosaic.system');

/**
 * Application entry point (the jar's Main-Class).
 *
 * Reads the component repository configuration - Repository.xml next to this
 * file, or the file named by the "config" query parameter, which plays the role
 * of the original command line argument - and builds the ApplicationRepository,
 * which instantiates and initializes all the components.
 */
export class Kernel {
    static async main(args = []) {
        System.out.println('Check "Help > Debug output" (the old debug.txt) for debug output');
        let configUrl;
        if (args.length > 0) {
            configUrl = new URL(args[0], document.baseURI);
        } else {
            configUrl = new URL('Repository.xml', import.meta.url);
        }
        let configText;
        try {
            const response = await fetch(configUrl);
            if (!response.ok) {
                throw new Error(response.status + ' ' + response.statusText);
            }
            configText = await response.text();
        } catch (e) {
            log.log(Level.SEVERE, 'No config file repository.xml found', e);
            log.log(Level.INFO, 'Usage: index.html[?config=<repository config file>]');
            throw e;
        }
        const config = new DOMParser().parseFromString(configText, 'application/xml');
        if (config.getElementsByTagName('parsererror').length > 0) {
            const e = new Error(config.getElementsByTagName('parsererror')[0].textContent);
            log.log(Level.SEVERE, 'Error parsing configuration file', e);
            throw e;
        }
        try {
            return await ApplicationRepository.load(config);
        } catch (t) {
            log.log(Level.SEVERE, 'Error creating application repository', t);
            throw t;
        }
    }
}
