public function foo() {
    type Foo object {

        *A;

        int age = 10 * 2;
        string name;
        public int status = 0;
        private float score;
        
        public function __init() {
        }

        *B;

        function getName() {
        }

        private remote function get() {
        
        }

        object {
            *A;
            int age = 10 * 2;
            string name;
            public int status = 0;
            private float score;

            public function __init() {
            }

            *B;

            function getName() {
            }
        } parent;
    };
}
