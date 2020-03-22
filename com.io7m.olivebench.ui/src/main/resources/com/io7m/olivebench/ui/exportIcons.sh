#!/bin/sh

EXPORT_WIDTH=32
EXPORT_HEIGHT=32

inkscape icons.svg \
--export-id-only \
--export-id=layer1 \
--export-png=playOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer3 \
--export-png=stopOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer4 \
--export-png=fastForwardOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer5 \
--export-png=fastBackwardOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer6 \
--export-png=fastToStartOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer7 \
--export-png=fastToEndOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer8 \
--export-png=cursorOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer9 \
--export-png=drawOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer10 \
--export-png=zoomOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer11 \
--export-png=addChannelOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer13 \
--export-png=removeChannelOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer14 \
--export-png=errorOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer15 \
--export-png=clockOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}

inkscape icons.svg \
--export-id-only \
--export-id=layer16 \
--export-png=diskOnDark.png \
--export-width=${EXPORT_WIDTH} --export-height=${EXPORT_HEIGHT}
