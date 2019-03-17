package org.edgexfoundry.pkg.cola;

import de.sick.sopas.device.api.*;
import de.sick.sopas.scl.*;
import de.sick.sopas.scl.IConnection.State;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public class TiM5Driver {
    private static final String SDD_URL = "TiM3xx-5xx-V2.59-sUdd.xml";

    private IConnection m_connection = null;
    private IDADevice m_device = null;

    private HashSet<IScandataListener> m_scandataListeners = new HashSet<IScandataListener>();

    private DAVariableListener l_scandataListener = new DAVariableListener() {
        public void valueChanged(IDAVariable source, IDANode value) {
            try {
                final int[] l_scandata = extractScandata(value);

                for (final IScandataListener l_listener : m_scandataListeners) {
                    l_listener.receiveScandata(l_scandata);
                }
            } catch (DAException ex) {
                ex.printStackTrace();
            }
        }

        ;
    };


    public TiM5Driver(final String ipAddress, final int tcpPort, final ColaDialect colaDialect) throws IOException {
        try {
            final DescriptionProvider l_descriptionProvider = new CIDDescriptionProvider(TiM5Driver.class.getResource(SDD_URL), true);

            final ColaTCPSettings l_settings = new ColaTCPSettings.Builder(InetAddress.getByName(ipAddress), tcpPort,
                    colaDialect).colaAddressingMode(ColaAddressingMode.BY_NAME).build();

            m_connection = new ConnectionFactory(l_descriptionProvider)
                    .createConnection(l_settings);
        } catch (UnknownHostException ex) {
            throw new IOException(ex);
        }
    }

    public void connect() throws IOException {
        if (State.ONLINE != m_connection.getState()) {
            m_connection.connect();
            m_device = m_connection.getRootDevice();
        }
    }

    public void disconnect() throws IOException {
        if (State.ONLINE == m_connection.getState()) {
            m_device = null;
            m_connection.disconnect();
        }
    }

    public int[] readScandata() throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable l_varScandata = m_device.getVariable("LMDscandata");
            final int[] l_scandata = extractScandata(l_varScandata.read());
            return l_scandata;
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public int readDistanceAhead() throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable l_varScandata = m_device.getVariable("LMDscandata");
            final int[] l_scandata = extractScandata(l_varScandata.read());
            final int l_idxAhead = l_scandata.length / 2;
            return l_scandata[l_idxAhead];
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public int readTemperature() throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable l_varTempDigBoard = m_device.getVariable("TempDigBoard");
            final IDANode l_nodeTempDigBoard = l_varTempDigBoard.read();
            return l_nodeTempDigBoard.getNumber().intValue();
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public String readSerialnumber() throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable l_varSerialnumber = m_device.getVariable("SerialNumber");
            final IDANode l_nodeSerialnumber = l_varSerialnumber.read();
            return l_nodeSerialnumber.getString();
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public String read(String variable) throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable idaVariable = m_device.getVariable(variable);
            final IDANode idaNode = idaVariable.read();
            return idaNode.getString();
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public float readOperatingHours() throws IOException {
        if (null == m_device) {
            throw new IllegalStateException();
        }

        try {
            final IDAVariable l_varOpHours = m_device.getVariable("OpHours");
            final IDANode l_nodeOpHours = l_varOpHours.read();

            // TiM's op hours counter is having the unit [1/10 h]. Therefore we have to divide by 10 in order to get the correct amount of hours.
            final float l_opHours = (float) l_nodeOpHours.getNumber().intValue() / 10.0f;
            return l_opHours;
        } catch (DAException ex) {
            throw new IOException(ex);
        }
    }

    public void registerScandataListener(final IScandataListener listener) throws IOException {
        try {
            if (m_scandataListeners.isEmpty()) {
                final IDAVariable l_varScandata = m_device.getVariable("LMDscandata");
                l_varScandata.addRefreshInstruction(DARefreshInstruction.EVENT);
                l_varScandata.addVariableListener(l_scandataListener);
            }

            m_scandataListeners.add(listener);
        } catch (DAInvalidTypeException ex) {
            throw new IOException(ex);
        }
    }

    public void unregisterScandataListener(final IScandataListener listener) throws IOException {
        try {
            m_scandataListeners.remove(listener);

            if (m_scandataListeners.isEmpty()) {
                final IDAVariable l_varScandata = m_device.getVariable("LMDscandata");
                l_varScandata.removeRefreshInstruction(DARefreshInstruction.EVENT);
                l_varScandata.removeVariableListener(l_scandataListener);
            }
        } catch (DAInvalidTypeException ex) {
            throw new IOException(ex);
        }
    }

    private int[] extractScandata(final IDANode scandataRootNode) throws DAException {
        final IDANode l_nodeDistanceValues = scandataRootNode.getChild("aDataChannel16/0/aData");

        final int[] l_ret = new int[l_nodeDistanceValues.getArrayLength()];
        final IDAArrayHelper l_arrayHelper = DAArrayHelperFactory.getInstance().createArrayHelper(m_device);

        l_arrayHelper.getArrayValue(l_nodeDistanceValues, l_ret, 0, l_ret.length);

        return l_ret;
    }
}
