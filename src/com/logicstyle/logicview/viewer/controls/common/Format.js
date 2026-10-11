/** java.text.DecimalFormat("##.##") */
export function formatDecimal2(v) {
    return String(Number(v.toFixed(2)));
}

/** DecimalFormat with 0..4 fraction digits and grouping, as used for labels. */
export function formatDecimal4(v) {
    return v.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 4 });
}
