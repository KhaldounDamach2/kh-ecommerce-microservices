sed -i -E 's/light-dark\(#FFFFFF, #121212\)/#FFFFFF/g' file.drawio.svg
sed -i -E 's/light-dark\(#ffffff, var\(--ge-dark-color, #121212\)\)/#ffffff/g' file.drawio.svg
sed -i -E 's/light-dark\(#000000, #ffffff\)/#000000/g' file.drawio.svg
sed -i -E 's/light-dark\(rgb\(255, 255, 255\), rgb\(18, 18, 18\)\)/rgb(255, 255, 255)/g' file.drawio.svg
sed -i -E 's/light-dark\(rgb\(0, 0, 0\), rgb\(255, 255, 255\)\)/rgb(0, 0, 0)/g' file.drawio.svg
