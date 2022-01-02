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
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

import java.awt.*;

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
      var dataWidth = BitWidth.create(8);
      final var data = (StateData) state.getData();
      if (data == null) return Value.createKnown(dataWidth, 0);
      return Value.createKnown(dataWidth, data.fileValues[2]);
    }

    @Override
    public boolean isInput(InstanceState state, Object option) {
      return true;
    }
  }

  private static class StateData extends ClockState implements InstanceData {

    private char[] fileValues = new char[] {0, 0, 0, 0, 0};
    private int currentAddress = 0;

    private final char[] dummyFile = new char[] {
      0x9e, 0xdd, 0x07, 0x09, 0x5e, 0xc5, 0xb9, 0x80, 0x10, 0x22, 0x02, 0x8c, 0xcb, 0xa4, 0xdb, 0x74,
      0xbe, 0x7e, 0x7d, 0xd6, 0x96, 0x4b, 0xf5, 0xad, 0xc6, 0x7b, 0x2d, 0x87, 0xdd, 0x7b, 0xff, 0x96,
      0x98, 0x8a, 0xf4, 0xbd, 0x1c, 0xce, 0xd6, 0x5e, 0xa8, 0xa0, 0x0c, 0xe1, 0x66, 0xd8, 0xa5, 0xc9,
      0x29, 0x6b, 0x40, 0x97, 0x48, 0xda, 0xe2, 0xf1, 0xf8, 0x8c, 0xe2, 0xda, 0xa8, 0x9a, 0x21, 0x1e,
      0xa2, 0x75, 0xd5, 0x71, 0x9f, 0xd1, 0x76, 0x70, 0x32, 0x92, 0x2f, 0x07, 0x8b, 0x90, 0xcb, 0x6d,
      0xed, 0xcb, 0x67, 0xa8, 0x56, 0x27, 0x36, 0x2c, 0xd7, 0x82, 0x63, 0x62, 0xc0, 0x4c, 0x5c, 0x4d,
      0x3c, 0x74, 0x41, 0x6f, 0x5f, 0x06, 0x66, 0x54, 0x7f, 0xf6, 0x7d, 0x20, 0x5e, 0xe3, 0x31, 0x96,
      0xed, 0x10, 0x3a, 0x98, 0x3c, 0x18, 0xea, 0x31, 0x18, 0xb6, 0x91, 0x23, 0xf3, 0xb6, 0x1a, 0xb1,
      0xe5, 0x0e, 0x56, 0x91, 0xf1, 0x8b, 0xb3, 0xfa, 0x70, 0x66, 0x54, 0x14, 0x0b, 0x51, 0xc8, 0x74,
      0x9f, 0x28, 0xb5, 0xe0, 0x93, 0x76, 0x63, 0x77, 0xb1, 0xf9, 0xbc, 0xf3, 0x70, 0x5e, 0xba, 0x23,
      0x19, 0x79, 0x47, 0x3f, 0xed, 0xb2, 0xa2, 0xa2, 0xd2, 0xed, 0xf1, 0xde, 0xfe, 0x15, 0x47, 0x3e,
      0x3f, 0x81, 0x8e, 0xe7, 0x5b, 0x02, 0x71, 0x16, 0x7c, 0x81, 0xcc, 0x95, 0x35, 0xaa, 0x9c, 0xc8,
      0x5f, 0xb0, 0xa5, 0x04, 0xc3, 0x1f, 0x6a, 0x2f, 0xc5, 0x08, 0x3e, 0xbb, 0xd1, 0x56, 0x6f, 0x1a,
      0x58, 0x4b, 0xc2, 0x07, 0x8b, 0x13, 0x5d, 0x60, 0x24, 0xb9, 0xa5, 0x59, 0x7e, 0x54, 0x9d, 0x02,
      0x0a, 0x59, 0xf0, 0xd1, 0x80, 0x24, 0x22, 0xdd, 0x32, 0x0e, 0x89, 0x6d, 0xe5, 0xc9, 0x67, 0x68,
      0x04, 0xe4, 0xfb, 0xef, 0x18, 0xc3, 0xc9, 0x83, 0xdd, 0x2a, 0xfa, 0xe1, 0x27, 0x5c, 0xf8, 0xa5
    };
    private final int dummyFileLength = dummyFile.length;

    public StateData(Object seed) {
      updateAddress(0);
    }

    public boolean offsetValid(int offset) {
      if (offset < -2 || offset > 2) return false;
      if (currentAddress + offset < 0) return false;
      if (currentAddress + offset >= dummyFileLength) return false;
      return true;
    }

    public void updateAddress(int new_address) {
      currentAddress = new_address;
      if (currentAddress >= dummyFileLength) currentAddress = dummyFileLength - 1;

      if (offsetValid(-2)) fileValues[0] = dummyFile[currentAddress - 2];
      if (offsetValid(-1)) fileValues[1] = dummyFile[currentAddress - 1];
      fileValues[2] = dummyFile[currentAddress];
      if (offsetValid(1)) fileValues[3] = dummyFile[currentAddress + 1];
      if (offsetValid(2)) fileValues[4] = dummyFile[currentAddress + 2];
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
    /*
    if (attr == StdAttr.APPEARANCE) {
      instance.recomputeBounds();
      updatePorts(instance);
    }
    */
  }


  @Override
  public void paintInstance(InstancePainter painter) {
    final var bds = painter.getBounds();
    final var xpos = bds.getX();
    final var ypos = bds.getY();
    final var g = painter.getGraphics();
    final var origFont = g.getFont();
    final var state = (StateData) painter.getData();
    //final var currentAddr = painter.getPortValue(ADDR).toLongValue();
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
    g.setFont(new Font(origFont.getFontName(), Font.PLAIN, 14));
    GraphicsUtil.drawText(g, "\u00BB " + painter.getAttributeValue(ATTR_FILENAME), xpos + 15, ypos + 40, GraphicsUtil.H_LEFT, GraphicsUtil.V_TOP);
    GraphicsUtil.drawText(g, "@ 0x" + StringUtil.toHexString(painter.getAttributeValue(ATTR_ADDRWIDTH).getWidth(), state.currentAddress), xpos + 15, ypos + 60, GraphicsUtil.H_LEFT, GraphicsUtil.V_TOP);

    // Current file snippet
    g.drawRect(xpos + totalWidth / 2 - 25, ypos + 145, 50, 40);
    GraphicsUtil.switchToWidth(g, 1);
    g.drawLine(xpos + 5, ypos + 150, xpos + totalWidth - 5, ypos + 150);
    g.drawLine(xpos + 5, ypos + 180, xpos + totalWidth - 5, ypos + 180);
    g.setFont(new Font(origFont.getFontName(), Font.BOLD, 18));
    GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[2]), xpos + totalWidth / 2, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
    g.setFont(new Font(origFont.getFontName(), Font.PLAIN, 14));
    if (state.offsetValid(-2)) GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[0]), xpos + totalWidth / 2 - 70, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
    if (state.offsetValid(-1)) GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[1]), xpos + totalWidth / 2 - 45, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
    if (state.offsetValid(1))GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[3]), xpos + totalWidth / 2 + 45, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);
    if (state.offsetValid(2))GraphicsUtil.drawText(g, StringUtil.toHexString(8, state.fileValues[4]), xpos + totalWidth / 2 + 70, ypos + 162, GraphicsUtil.H_CENTER, GraphicsUtil.H_CENTER);

    // Port descriptors
    g.setFont(origFont);
    GraphicsUtil.drawText(g, "ADDR", xpos + addressPortX + 20, ypos + addressPortY, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
    GraphicsUtil.drawText(g, "CLK", xpos + clockPortX + 20, ypos + clockPortY, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
    GraphicsUtil.drawText(g, "OUT", xpos + outPortX - 20, ypos + outPortY, GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);

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
      data = new StateData(0);
      state.setData(data);
    }

    final var dataWidth = 8;
    Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
    final var triggered = data.updateClock(state.getPortValue(CLK), triggerType);

    if (triggered) {
      data.updateAddress((int)state.getPortValue(ADDR).toLongValue());
    }

    state.setPort(OUT, Value.createKnown(dataWidth, data.fileValues[2]), 1);
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
