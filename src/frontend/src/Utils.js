export function handleRestResponse(res) {
    if (res.ok) {
        return res.json();
    } else {
        throw res;
    }
}

export function handleRawResponse(res) {
    if (res.ok) {
        return res.text();
    } else {
        throw res;
    }
}