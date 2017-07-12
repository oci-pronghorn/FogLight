for dir in */
do
    ( cd $dir && bash includeFile gen.md>README.md )
done