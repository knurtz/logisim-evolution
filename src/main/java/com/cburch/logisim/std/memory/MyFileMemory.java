/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.memory;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.*;
import com.cburch.logisim.fpga.designrulecheck.netlistComponent;
import com.cburch.logisim.gui.icons.RandomIcon;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.MessageBox;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.JDialogOk;
import com.cburch.logisim.util.StringUtil;

import java.awt.*;
import java.io.RandomAccessFile;
import java.nio.file.*;

public class MyFileMemory extends InstanceFactory {
  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all tools.
   */
  public static final String _ID = "FileMemory";

  public static class Logger extends InstanceLogger {
    @Override
    public String getLogName(InstanceState state, Object option) {
      final var ret = state.getAttributeValue(StdAttr.LABEL);
      return ret != null && !ret.equals("") ? ret : null;
    }

    @Override
    public BitWidth getBitWidth(InstanceState state, Object option) {
      return BitWidth.create(8);
    }

    @Override
    public Value getLogValue(InstanceState state, Object option) {
      final var data = (StateData) state.getData();
      if (data == null) return Value.createKnown(8, 0);
      return Value.createKnown(8, data.fileValues[2]);
    }

    @Override
    public boolean isInput(InstanceState state, Object option) {
      return true;
    }
  }

  private static class StateData extends ClockState implements InstanceData {

    private char[] fileValues;

    private int currentAddress;
    private RandomAccessFile currentFile;
    public String currentFilePathString;
    public Path currentFilePath;
    public String currentFileName;
    private int currentFileLength;

    public StateData(String filepath, int offset) {
      reset(filepath, offset);
    }

    public boolean offsetValid(int offset) {
      int temp = currentAddress + offset;
      return (temp >= 0 && temp < currentFileLength);
    }

    public void reset(String filepath, int offset) {
      // reset member variables
      fileValues = new char[] {0, 0, 0, 0, 0};
      currentAddress = 0;
      currentFileLength = 0;

      // try to open provided file
      try {
        currentFilePathString = filepath;
        currentFilePath = Paths.get(currentFilePathString);
        currentFileName = currentFilePath.getFileName().toString();
        reloadFile();
        // reset pointer to the provided offset
        updateAddress(offset);
      }
      catch (Exception e) {
        currentFilePath = null;
        currentFileName = "filename error";
      }
    }

    public boolean reloadFile() {
      try {
        currentFile = new RandomAccessFile(currentFilePath.toString(), "r");
        currentFileLength = (int) currentFile.length();
        return true;
      }
      catch (Exception e) {
        currentFile = null;
        return false;
      }
    }

    public void updateAddress(int new_address) {
      currentAddress = new_address;
      if (currentAddress >= currentFileLength) currentAddress = currentFileLength - 1;
      if (currentAddress < 0) currentAddress = 0;

      try {
        switch (currentAddress) {
          case 0: currentFile.seek(0);
          break;
          case 1: currentFile.seek((long) currentAddress - 1);
          break;
          default: currentFile.seek((long) currentAddress - 2);
        }

        for (int i = -2; i <= 2; i++)
          if (offsetValid(i))
            fileValues[i + 2] = (char) currentFile.read();
      }
      catch (Exception e) {
        fileValues = new char[] {0, 0, 0, 0, 0};
      }
    }
  }

  public static Attribute<BitWidth> ATTR_ADDRWIDTH = Attributes.forBitWidth("addrWidth", S.getter("ramAddrWidthAttr"), 1, 24);
  public static Attribute<String> ATTR_FILENAME = Attributes.forString("filename", S.getter("fileMemoryFilename"));

  public static final AttributeOption LOCATION_ABSOLUTE =
    new AttributeOption(1, S.getter("fileMemoryLocationAbsolute"));
  public static final AttributeOption LOCATION_RELATIVE =
    new AttributeOption(2, S.getter("fileMemoryLocationRelative"));
  public static final Attribute<AttributeOption> LOCATION_TYPE =
    Attributes.forOption("locationType", S.getter("fileMemoryLocationType"), new AttributeOption[] {LOCATION_ABSOLUTE, LOCATION_RELATIVE});

  public static final int OUT = 0;
  public static final int ADDR = 1;
  public static final int CLK = 2;
  public static final int UNUSED = 3;

  private final int totalWidth = 180;
  private final int totalHeight = 200;

  private final int outPortX = totalWidth + 10;
  private final int outPortY = totalHeight / 2;
  private final int addressPortX = -10;
  private final int addressPortY = totalHeight / 2;
  private final int clockPortX = -10;
  private final int clockPortY = totalHeight / 2 + 20;

  public MyFileMemory() {
    super(_ID, S.getter("fileMemoryComponentName"));
    setAttributes(
        new Attribute[] {
          ATTR_ADDRWIDTH,
          StdAttr.EDGE_TRIGGER,
          ATTR_FILENAME,
          LOCATION_TYPE,
          StdAttr.LABEL,
          StdAttr.LABEL_FONT,
        },
        new Object[] {
          BitWidth.create(24),
          StdAttr.TRIG_RISING,
          "foobar.bin",
          LOCATION_ABSOLUTE,
          "",
          StdAttr.DEFAULT_LABEL_FONT,
        });
    setKeyConfigurator(new BitWidthConfigurator(ATTR_ADDRWIDTH));

    setOffsetBounds(Bounds.create(0, 0, totalWidth, totalHeight));
    setIcon(new RandomIcon());
    setInstanceLogger(Logger.class);
  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrs) {
    return Bounds.create(0, 0, totalWidth, totalHeight);
  }

  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    updatePorts(instance);
    final var bds = instance.getBounds();
    instance.setTextField(
        StdAttr.LABEL,
        StdAttr.LABEL_FONT,
        bds.getX() + bds.getWidth() / 2,
        bds.getY() - 5,
        GraphicsUtil.H_CENTER,
        GraphicsUtil.V_BASELINE);
  }

  private void updatePorts(Instance instance) {
    final var ps = new Port[3];

    ps[OUT] = new Port(outPortX, outPortY, Port.OUTPUT, 8);
    ps[ADDR] = new Port(addressPortX, addressPortY, Port.INPUT, ATTR_ADDRWIDTH);
    ps[CLK] = new Port(clockPortX, clockPortY, Port.INPUT, 1);

    instance.setPorts(ps);
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == ATTR_FILENAME || attr == LOCATION_TYPE) {
      // causes call to propagate()
      instance.fireInvalidated();
    }
  }


  @Override
  public void paintInstance(InstancePainter painter) {
    final var bds = painter.getBounds();
    final var xpos = bds.getX();
    final var ypos = bds.getY();
    final var g = painter.getGraphics();
    final var origFont = g.getFont();
    final var state = (StateData) painter.getData();
    GraphicsUtil.switchToWidth(g, 2);

    // Label & outer box
    painter.drawLabel();
    g.drawRect(xpos, ypos, totalWidth, totalHeight);

    // data output stubby
    g.drawLine(xpos + outPortX, ypos + outPortY, xpos + outPortX - 10, ypos + outPortY);
    // address input stubby
    g.drawLine(xpos + addressPortX, ypos + addressPortY, xpos + addressPortX + 10, ypos + addressPortY);
    // clock input stubby
    if (painter.getAttributeValue(StdAttr.EDGE_TRIGGER).equals(StdAttr.TRIG_FALLING)) {
      g.drawOval(xpos + clockPortX, ypos + clockPortY -5, 10, 10);
    } else {
      g.drawLine(xpos + clockPortX, ypos + clockPortY, xpos + clockPortX + 10, ypos + clockPortY);
    }

    // Headline
    g.setFont(new Font(origFont.getFontName(), Font.PLAIN, 18));
    GraphicsUtil.drawText(g, "FileMemory", xpos + totalWidth / 2, ypos + 10, GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
    g.drawLine(xpos + 0, ypos + 30, xpos + totalWidth, ypos + 30);

    // filename @ address
    if (state != null) {
      g.setFont(new Font(origFont.getFontName(), Font.PLAIN, 14));
      if (state.currentFile == null) g.setColor(Color.RED);
      GraphicsUtil.drawText(g, "\u00BB " + state.currentFileName, xpos + 15, ypos + 40, GraphicsUtil.H_LEFT, GraphicsUtil.V_TOP);
      g.setColor(Color.BLACK);
      GraphicsUtil.drawText(g, "@ 0x" + StringUtil.toHexString(painter.getAttributeValue(ATTR_ADDRWIDTH).getWidth(), state.currentAddress), xpos + 15, ypos + 60, GraphicsUtil.H_LEFT, GraphicsUtil.V_TOP);
    }

    // Current file snippet
    g.drawRect(xpos + totalWidth / 2 - 25, ypos + 145, 50, 40);
    GraphicsUtil.switchToWidth(g, 1);
    g.drawLine(xpos + 5, ypos + 150, xpos + totalWidth - 5, ypos + 150);
    g.drawLine(xpos + 5, ypos + 180, xpos + totalWidth - 5, ypos + 180);

    if (state != null) {
      g.setFont(new Font(origFont.getFontName(), Font.BOLD, 18));
      GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[2]), xpos + totalWidth / 2, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
      g.setFont(new Font(origFont.getFontName(), Font.PLAIN, 14));
      if (state.offsetValid(-2))
        GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[0]), xpos + totalWidth / 2 - 70, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
      if (state.offsetValid(-1))
        GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[1]), xpos + totalWidth / 2 - 45, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
      if (state.offsetValid(1))
        GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[3]), xpos + totalWidth / 2 + 45, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
      if (state.offsetValid(2))
        GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[4]), xpos + totalWidth / 2 + 70, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
    }

    // Port descriptors
    g.setFont(origFont);
    GraphicsUtil.drawText(g, "ADDR", xpos + addressPortX + 20, ypos + addressPortY, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
    GraphicsUtil.drawText(g, "CLK", xpos + clockPortX + 20, ypos + clockPortY, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
    GraphicsUtil.drawText(g, "OUT", xpos + outPortX - 20, ypos + outPortY, GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
    g.setColor(Color.GRAY);
    GraphicsUtil.drawText(g, "RDY", xpos + outPortX - 20, ypos + outPortY + 20, GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
    g.setColor(Color.BLACK);

    // Ports
    painter.drawPort(OUT);
    painter.drawPort(ADDR);
    painter.drawPort(CLK);
    painter.drawClockSymbol(xpos + clockPortX + 10, ypos + clockPortY);
  }

  @Override
  public void paintGhost(InstancePainter painter) {
    final var bds = painter.getBounds();
    final var xpos = bds.getX();
    final var ypos = bds.getY();
    final var g = painter.getGraphics();
    GraphicsUtil.switchToWidth(g, 2);
    // Outer box
    g.drawRect(xpos, ypos, totalWidth, totalHeight);
    // Headline
    var oldFont = g.getFont();
    g.setFont(new Font(oldFont.getFontName(), Font.PLAIN, 18));
    GraphicsUtil.drawText(g, "FileMemory", xpos + totalWidth / 2, ypos + 10, GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
    // data output stubby
    g.drawLine(xpos + outPortX, ypos + outPortY, xpos + outPortX - 10, ypos + outPortY);
    // address input stubby
    g.drawLine(xpos + addressPortX, ypos + addressPortY, xpos + addressPortX + 10, ypos + addressPortY);
    // clock input stubby
    if (painter.getAttributeValue(StdAttr.EDGE_TRIGGER).equals(StdAttr.TRIG_FALLING)) {
      g.drawOval(xpos + clockPortX, ypos + clockPortY -5, 10, 10);
    } else {
      g.drawLine(xpos + clockPortX, ypos + clockPortY, xpos + clockPortX + 10, ypos + clockPortY);
    }
  }

  @Override
  public void propagate(InstanceState state) {
    var data = (StateData) state.getData();
    if (data == null) {
      data = new StateData(state.getAttributeValue(ATTR_FILENAME), (int) state.getPortValue(ADDR).toLongValue());
      state.setData(data);
    }

    // check trigger on clock pin
    if (data.updateClock(state.getPortValue(CLK), state.getAttributeValue(StdAttr.EDGE_TRIGGER)))
      data.updateAddress((int) state.getPortValue(ADDR).toLongValue());

    // check for changed filename
    if (!data.currentFilePathString.equals(state.getAttributeValue(ATTR_FILENAME)))
      data.reset(state.getAttributeValue(ATTR_FILENAME), 0);

    state.setPort(OUT, Value.createKnown(8, data.fileValues[2]), 1);
  }

  @Override
  public boolean checkForGatedClocks(netlistComponent comp) {
    return true;
  }

  @Override
  public int[] clockPinIndex(netlistComponent comp) {
    return new int[] {CLK};
  }
}
