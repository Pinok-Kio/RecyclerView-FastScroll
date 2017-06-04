package com.serega.fastscroller;

import android.support.annotation.Nullable;

/**
 * @author S.A.Bobrischev
 *         Developed by Magora Team (magora-systems.com). 2017.
 */
public interface FastScrollAdapter {

    @Nullable
    String getLetter(int position);

    int getPositionForScrollProgress(float progress);
}
