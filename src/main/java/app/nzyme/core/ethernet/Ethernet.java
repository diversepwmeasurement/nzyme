package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeLeader;
import app.nzyme.core.ethernet.dns.DNS;

public class Ethernet {

    private final NzymeLeader nzyme;

    private final DNS dns;

    public Ethernet(NzymeLeader nzyme) {
        this.nzyme = nzyme;
        this.dns = new DNS(this);
    }

    public NzymeLeader getNzyme() {
        return this.nzyme;
    }

    public DNS dns() {
        return dns;
    }

}