package biz.minecraft.launcher.game.updater.json;

import biz.minecraft.launcher.OperatingSystem;
import com.google.gson.annotations.SerializedName;

/**
 * Minecraft Version's library compatibility rule.
 */
public class CompatibilityRule
{
    private Action action;
    private OSRestriction os;

    public CompatibilityRule() {
        this.action = Action.ALLOW;
    }

    public CompatibilityRule(final CompatibilityRule compatibilityRule) {
        this.action = Action.ALLOW;
        this.action = compatibilityRule.action;
        if (compatibilityRule.os != null) {
            this.os = new OSRestriction(compatibilityRule.os);
        }
    }

    /**
     *
     *
     * @return enum action, or null if the rule's value 'os' exists and do not match user's operating system.
     */
    public Action getAppliedAction() {
        if (this.os != null && !this.os.isCurrentOperatingSystem()) {
            return null;
        }
        return this.action;
    }

    public Action getAction() {
        return this.action;
    }

    public OSRestriction getOs() {
        return this.os;
    }

    @Override
    public String toString() {
        return "Rule{action=" + this.action + ", os=" + this.os + '}';
    }

    public enum Action
    {
        @SerializedName("allow") ALLOW,
        @SerializedName("disallow") DISALLOW
    }

    /**
     * Minecraft Version's library compatibility rule possible operating system restriction.
     */
    public class OSRestriction
    {
        private OperatingSystem name;

        public OSRestriction() {
        }

        public OperatingSystem getName() {
            return this.name;
        }

        public OSRestriction(final OSRestriction osRestriction) {
            this.name = osRestriction.name;
        }

        /**
         *
         *
         * @return true, or false if the rule's value 'os: {name}' exists and do not match user's operating system.
         */
        public boolean isCurrentOperatingSystem() {
            if (this.name != null && this.name != OperatingSystem.getCurrentPlatform()) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "OSRestriction{name=" + this.name + "}";
        }
    }
}

