/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.knurtz.logisim.memory;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

import java.util.List;

import static com.cburch.logisim.std.Strings.S;

public class MyMemoryLibrary extends Library {
  /**
   * Unique identifier of the library, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all libraries.
   */
  public static final String _ID = "MyMemory";

  protected static final int DELAY = 5;

  private static final FactoryDescription[] DESCRIPTIONS = {
    new FactoryDescription(MyFileMemory.class, S.getter("fileMemoryComponentName"), "random.gif"),
  };

  private List<Tool> tools = null;

  @Override
  public String getDisplayName() {
    return S.get("MemoryLibrary");
  }

  @Override
  public List<Tool> getTools() {
    if (tools == null) {
      tools = FactoryDescription.getTools(MyMemoryLibrary.class, DESCRIPTIONS);
    }
    return tools;
  }
}
