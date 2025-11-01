    package se233.adpro2.model;

    import se233.adpro2.Main;
    import se233.adpro2.util.AnimatedSprite;
    import se233.adpro2.util.SpriteSheet;

    /**
     * Defines the playable Contra character Bill Rizer
     * using separated sprite images stored in assets/sprites/
     */
    public class PlayableCharacter {

        public final String name;

        // Animation states
        public final AnimatedSprite idle;
        public final AnimatedSprite run;
        public final AnimatedSprite jump;
        public final AnimatedSprite prone;
        public final AnimatedSprite shoot;
        private AnimatedSprite current;

        // state flags
        private boolean onGround = true;
        private boolean moving;
        private boolean jumping;
        private boolean proneFlag;
        private boolean shootingFlag;

        // Render scale
        public final double scale;

        public PlayableCharacter(String name, double scale) {
            this.name = name;
            this.scale = scale;

            this.idle  = new AnimatedSprite(
                    new SpriteSheet(Main.class.getResource("/assets/sprites/AvatarBill-move.png").toExternalForm(), 40, 60),
                    0, new int[]{0, 1}, 6);
            this.run   = new AnimatedSprite(
                    new SpriteSheet(Main.class.getResource("/assets/sprites/AvatarBill-idle.png").toExternalForm(), 40, 60),
                    0, new int[]{0, 1, 2, 3}, 12);
            this.jump  = new AnimatedSprite(
                    new SpriteSheet(Main.class.getResource("/assets/sprites/AvatarBill-jump.png").toExternalForm(), 40, 60),
                    0, new int[]{0, 1}, 8);
            this.prone = new AnimatedSprite(
                    new SpriteSheet(Main.class.getResource("/assets/sprites/AvatarBill-prone.png").toExternalForm(), 40, 60),
                    0, new int[]{0}, 1);
            this.shoot = new AnimatedSprite(
                    new SpriteSheet(Main.class.getResource("/assets/sprites/AvatarBill-shoot.png").toExternalForm(), 40, 60),
                    0, new int[]{0, 1}, 10);
        }

        /** Factory for Bill */
        public static PlayableCharacter BILL() {
            return new PlayableCharacter("Bill Rizer", 2.5);
        }
    }
