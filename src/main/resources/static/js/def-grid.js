const GRID_DEF_HTML = {
    A: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label  class="grid-title">A</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div data-val="A-1" class="grid-btn">A-1</div>
            <div data-val="A-2" class="grid-btn">A-2</div>
            <div data-val="A-3" class="grid-btn">A-3</div>
            <div data-val="A-4" class="grid-btn">A-4</div>
            <div data-val="A-5" class="grid-btn">A-5</div>
            <div data-val="A-6" class="grid-btn">A-6</div>
            <div data-val="A-7" class="grid-btn">A-7</div>
            <div data-val="A-8" class="grid-btn">A-8</div>
        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    B: `<div class="col grid-col">
        <div class="row grid-row">
            <div class="col-1">
            <div class="row">
                <div class="col">
                <label class="grid-title">B</label>
                </div>
            </div>
            </div>
            <div class="col-10 flex-wrap table-container grid-btns">
                <div data-val="B-1" class="grid-btn">B-1</div>
                <div data-val="B-2" class="grid-btn t">B-2</div>
                <div data-val="B-3" class="grid-btn t">B-3</div>
            </div>
            <div class="col-1"></div>
        </div>
    </div>`,
    C: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label class="grid-title">C</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div class="row">
            <div class="col">
                <div data-val="C-1" class="grid-btn t">C-1</div>
                <div data-val="C-2" class="grid-btn t">C-2</div>
                <div data-val="C-3" class="grid-btn t">C-3</div>
                <div data-val="C-4" class="grid-btn t">C-4</div>
                <div data-val="C-5" class="grid-btn t">C-5</div>
                <div data-val="C-6" class="grid-btn t">C-6</div>
                <div data-val="C-7" class="grid-btn t">C-7</div>
                <div data-val="C-8" class="grid-btn t">C-8</div>
            </div>
            </div>
            <div class="row">
            <div class="col">
                <div data-val="C-9" class="grid-btn t">C-9</div>
                <div data-val="C-10" class="grid-btn t">C-10</div>
                <div data-val="C-11" class="grid-btn t">C-11</div>
                <div data-val="C-12" class="grid-btn t">C-12</div>
                <div data-val="C-13" class="grid-btn t">C-13</div>
                <div data-val="C-14" class="grid-btn t">C-14</div>
                <div data-val="C-15" class="grid-btn t">C-15</div>
                <div data-val="C-16" class="grid-btn t">C-16</div>
            </div>
            </div>

        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    D: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label class="grid-title">D</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div class="row">
            <div class="col">
                <div data-val="D-1" class="grid-btn t">D-1</div>
                <div data-val="D-2" class="grid-btn t">D-2</div>
                <div data-val="D-3" class="grid-btn t">D-3</div>
                <div data-val="D-4" class="grid-btn t">D-4</div>
                <div data-val="D-5" class="grid-btn t">D-5</div>
                <div data-val="D-6" class="grid-btn t">D-6</div>
                <div data-val="D-7" class="grid-btn t">D-7</div>
            </div>
            </div>
            <div class="row">
            <div class="col">
                <div data-val="D-8" class="grid-btn t">D-8</div>
                <div data-val="D-9" class="grid-btn t">D-9</div>
                <div data-val="D-10" class="grid-btn t">D-10</div>
                <div data-val="D-11" class="grid-btn t">D-11</div>
                <div data-val="D-12" class="grid-btn t">D-12</div>
                <div data-val="D-13" class="grid-btn t">D-13</div>
                <div data-val="D-14" class="grid-btn t">D-14</div>
            </div>
            </div>
        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    E: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label class="grid-title">E</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div class="row">
            <div class="col">
                <div data-val="E-1" class="grid-btn">E-1</div>
                <div data-val="E-2" class="grid-btn">E-2</div>
                <div data-val="E-3" class="grid-btn">E-3</div>
                <div data-val="E-4" class="grid-btn">E-4</div>
            </div>
            </div>

        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    F: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label class="grid-title">F</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div data-val="F-1" class="grid-btn t">F-1</div>
            <div data-val="F-2" class="grid-btn">F-2</div>
            <div data-val="F-3" class="grid-btn">F-3</div>
            <div data-val="F-4" class="grid-btn">F-4</div>

        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    G: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label class="grid-title">G</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div class="row">
            <div class="col">
                <div data-val="G-1" class="grid-btn t">G-1</div>
                <div data-val="G-2" class="grid-btn t">G-2</div>
            </div>
            </div>
        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
    H: `<div class="col grid-col">
        <div class="row grid-row">
        <div class="col-1">
            <div class="row">
            <div class="col">
                <label  class="grid-title">H</label>
            </div>
            </div>
        </div>
        <div class="col-10 flex-wrap table-container grid-btns">
            <div data-val="H-1" class="grid-btn">H-1</div>
            <div data-val="H-2" class="grid-btn">H-2</div>
            <div data-val="H-3" class="grid-btn">H-3</div>
            <div data-val="H-4" class="grid-btn">H-4</div>
            <div data-val="H-5" class="grid-btn">H-5</div>
            <div data-val="H-6" class="grid-btn">H-6</div>
            <div data-val="H-7" class="grid-btn">H-7</div>
            <div data-val="H-8" class="grid-btn">H-8</div>

        </div>
        <div class="col-1"></div>
        </div>
    </div>`,
};