const gulp = require('gulp');
const fs = require('fs');
const minify = require('gulp-minify');
const watch = require('gulp-watch');
const postcss = require('gulp-postcss');
const tailwindcss = require('tailwindcss');
const autoprefixer = require('autoprefixer');
const rename = require('gulp-rename');
const concat = require('gulp-concat');
const propertiesReader = require('properties-reader');
const browserify = require('browserify');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');

const SRC_CSS = 'src/web/css';
const SRC_JS = 'src/web/js';
const DEST_CSS = 'src/web/webapp/static/css';
const DEST_JS = 'src/web/webapp/static/js';

const appVersion = propertiesReader('version.properties').getRaw('version');
console.log(`App version is '${appVersion}'`);

gulp.task('clean', function () {
    return new Promise(function (resolve, reject) {
        fs.rmSync(DEST_CSS, {recursive: true, force: true});
        fs.rmSync(DEST_JS, {recursive: true, force: true});
        resolve();
    });
});

gulp.task('build-js', function () {
    return browserify(`${SRC_JS}/app.js`)
        .bundle()
        .pipe(source('app.js'))
        .pipe(buffer())
        .pipe(
            minify({
                ext: {min: `.${appVersion}.js`},
                noSource: true,
                ignoreFiles: ['*.min.js'],
                mangle: true,
            })
        )
        .pipe(gulp.dest(DEST_JS));
});

gulp.task('build-css', gulp.parallel(
    function () {
        return gulp
            .src([`${SRC_CSS}/main.css`, `${SRC_CSS}/!(main).css`])
            .pipe(concat('style.css'))
            .pipe(
                postcss([
                    require('postcss')(tailwindcss(`tailwind.config.js`)),
                    autoprefixer({
                        plugins: {
                            tailwindcss: {},
                            autoprefixer: {},
                        },
                    }),
                    require('cssnano')({
                        preset: 'default',
                    }),
                ])
            )
            .pipe(rename(`style.${appVersion}.css`))
            .pipe(gulp.dest(DEST_CSS));
    }
));

gulp.task('build', gulp.series(
    'build-js',
    'build-css'
));

gulp.task('cleanBuild', gulp.series(
    'clean',
    'build'
));

gulp.task('watch', () => {
    watch(
        [
            `${SRC_CSS}/**/*.css`,
            `${SRC_JS}/**/*.js`,
            "src/web/webapp/views/**/*.peb",
            "tailwind.config.js",
        ],
        {ignoreInitial: false},
        gulp.series('build')
    );
});
