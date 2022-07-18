package fr.naruse.servermanager.bukkit.utils;

public class Heads {

    public static final Head RED_CHEST = new Head("http://textures.minecraft.net/texture/5b3348a61cffc79eb42d849d378229432aa81d19856b25feaac7538487137a5");
    public static final Head BLUE_CHEST = new Head("http://textures.minecraft.net/texture/57844a4554f9363d659906d6312818a1122a1f9a92a23262b1e67702232bb7");
    public static final Head BLUE_MILKED_BUCKET = new Head("http://textures.minecraft.net/texture/6f2cdff62f05dbe7db7fd191949183e2764d68f37726db356aa461a58c54a");
    public static final Head BACK = new Head("http://textures.minecraft.net/texture/8652e2b936ca8026bd28651d7c9f2819d2e923697734d18dfdb13550f8fdad5f");
    public static final Head MINUS = new Head("http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46");
    public static final Head PLUS = new Head("http://textures.minecraft.net/texture/ac731c3c723f67d2cfb1a1192b947086fba32aea472d347a5ed5d7642f73b");
    public static final Head NUMBER_0 = new Head("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27");
    public static final Head NUMBER_1 = new Head("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530");
    public static final Head NUMBER_2 = new Head("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847");
    public static final Head NUMBER_3 = new Head("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5");
    public static final Head NUMBER_4 = new Head("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5");
    public static final Head NUMBER_5 = new Head("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2");
    public static final Head NUMBER_6 = new Head("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab");
    public static final Head NUMBER_7 = new Head("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9");
    public static final Head NUMBER_8 = new Head("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5");
    public static final Head NUMBER_9 = new Head("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840");
    public static final Head EARTH = new Head("http://textures.minecraft.net/texture/c69196b330c6b8962f23ad5627fb6ecce472eaf5c9d44f791f6709c7d0f4dece");
    public static final Head CHEST_HIGH_CONTRAST = new Head("http://textures.minecraft.net/texture/95732d3e57b9a62159b35d2754574452a32b4c221fc4fd363bff522ff04a577e");
    public static final Head DEATH_SKULL = new Head("http://textures.minecraft.net/texture/eb6ac4668f27c3dd367ca288e3260c98720135ad286785f6d1b5f4f56b91de1");
    public static final Head HEARTS = new Head("http://textures.minecraft.net/texture/2c8fb637d6e1a7ba8fa97ee9d2915e843e8ec790d8b7bf6048be621ee4d59fba");
    public static final Head SPY = new Head("http://textures.minecraft.net/texture/3559da40a9eeefa253693a49812dd269f45ec30628a22df9e5f4e85bb9cbb65");
    public static final Head LUCKY = new Head("http://textures.minecraft.net/texture/519d28a8632fa4d87ca199bbc2e88cf368dedd55747017ae34843569f7a634c5");

    private Heads() {
    }

    public static class Head {

        private final String url;

        public Head(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }
    }
}
