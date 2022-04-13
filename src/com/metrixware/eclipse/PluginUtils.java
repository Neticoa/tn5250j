/**
 *
 */
package com.metrixware.eclipse;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class PluginUtils {

    private PluginUtils() {}

    public static int getWidthForNumCharacterrs(final Text text, final int numChars) {
        final GC gc = new GC(text);
        try {
            final FontMetrics fm = gc.getFontMetrics();
            return Math.round(numChars * (int) fm.getAverageCharacterWidth());
        } finally {
            gc.dispose();
        }
    }

    public static URL locate(final String filePath) {
        final IPath uriPath = new Path("/plugin").append(Activator.BUNDLE_SYMBOLIC_NAME).append(filePath); //$NON-NLS-1$
        URL url;
        try {
            final URI uri = new URI("platform", null, uriPath.toString(), null); //$NON-NLS-1$
            url = uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }

        // look for the resource
        final URL fullPathString = FileLocator.find(url);
        if (fullPathString == null) {
            // If not found, reinterpret imageFilePath as full URL.
            // This is unspecified, but apparently widely-used, see bug 395126.
            try {
                url = new URL(filePath);
            } catch (final MalformedURLException e) {
                return null;
            }
        }
        return url;
    }

    public static <T extends Control> T addLabelAndLayout(final Composite parent, final Label label, final T child) {
        final GridData firstRowData = new GridData();
        firstRowData.horizontalAlignment = SWT.LEFT;
        label.setLayoutData(firstRowData);

        final GridData secondRowData = new GridData();
        secondRowData.horizontalAlignment = SWT.LEFT;
        child.setLayoutData(secondRowData);

        return child;
    }

    /**
     * @param imageName image name.
     * @return image descriptor for image with given name.
     */
    public static ImageDescriptor createImageDescriptor(final String imageName) {
        return ImageDescriptor.createFromURL(PluginUtils.locate("/images/" + imageName));//$NON-NLS$
    }
}
